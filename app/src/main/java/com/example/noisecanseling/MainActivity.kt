package com.example.noisecanseling

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.noisecanseling.network.RetrofitClient
import com.example.noisecanseling.network.TokenManager
import com.example.noisecanseling.ui.theme.NoisecanselingTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // 앱 시작 시 저장된 토큰으로 로그인 상태 복원
        isLoggedIn = TokenManager.isLoggedIn()

        setContent {
            NoisecanselingTheme {
                AppTheme(isSystemDark = isSystemInDarkTheme()) {
                    Surface(modifier = Modifier.fillMaxSize(), color = AppBg) {
                        AppNavigation()
                    }
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    var backPressedOnce by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // 메인에서 두 번 눌러야 종료
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route
    if (currentRoute == "main") {
        BackHandler {
            if (backPressedOnce) {
                (context as? android.app.Activity)?.finish()
            } else {
                backPressedOnce = true
                Toast.makeText(context, "한 번 더 누르면 종료됩니다", Toast.LENGTH_SHORT).show()
            }
        }
    }
    LaunchedEffect(backPressedOnce) {
        if (backPressedOnce) { delay(2000L); backPressedOnce = false }
    }

    var pendingUploadUri by remember { mutableStateOf<Uri?>(null) }
    val scope = rememberCoroutineScope()

    NavHost(navController = navController, startDestination = "splash") {

        composable("splash") {
            LaunchedEffect(Unit) {
                delay(1800L)
                navController.navigate("main") {
                    popUpTo("splash") { inclusive = true }
                }
            }
            SplashScreen()
        }

        composable("main") {
            MainApp(
                onLoginRequired = { navController.navigate("login") },
                onNavigateToPlayer = { index, autoPlay -> navController.navigate("player/$index?autoPlay=$autoPlay") },
                onNavigateToPlaylist = { navController.navigate("playlist") },
                onNavigateToUpload = { navController.navigate("upload_initial") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }

        composable("login") {
            LoginScreen(
                onSignUpClick = { navController.navigate("signup") },
                onFindClick = { navController.navigate("find") },
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("main") { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("signup") {
            SignUpScreen(
                onBackClick = { navController.popBackStack() },
                onSignUpComplete = { navController.navigate("complete") }
            )
        }

        composable("complete") {
            CompleteScreen(onGoToLoginClick = {
                navController.navigate("login") {
                    popUpTo("login") { inclusive = true }
                }
            })
        }

        composable("find") {
            FindPasswordScreen(
                onBackClick = { navController.popBackStack() },
                onLoginClick = { navController.popBackStack() }
            )
        }

        composable(
            route = "player/{songIndex}?autoPlay={autoPlay}",
            arguments = listOf(
                navArgument("songIndex") { type = NavType.IntType },
                navArgument("autoPlay") { type = NavType.BoolType; defaultValue = false }
            )
        ) { backStackEntry ->
            val index = backStackEntry.arguments?.getInt("songIndex") ?: 0
            val autoPlay = backStackEntry.arguments?.getBoolean("autoPlay") ?: false
            PlayerScreen(
                initialSongIndex = index,
                autoPlay = autoPlay,
                onBackClick = { navController.popBackStack() },
                onPlaylistClick = { navController.navigate("playlist") }
            )
        }

        composable("playlist") {
            MyPlaylistScreen(
                onBackClick = { navController.popBackStack() },
                onPlayClick = { index ->
                    navController.navigate("player/$index?autoPlay=true") {
                        popUpTo("playlist") { inclusive = true }
                    }
                }
            )
        }

        composable("upload_initial") {
            UploadInitialScreen(
                onBackClick = { navController.popBackStack() },
                onUploadClick = { uri, name ->
                    pendingUploadUri = uri
                    navController.navigate("upload_detail/${name.ifBlank { "file" }}")
                }
            )
        }

        composable(
            route = "upload_detail/{fileName}",
            arguments = listOf(navArgument("fileName") { type = NavType.StringType })
        ) { backStackEntry ->
            val fileName = backStackEntry.arguments?.getString("fileName") ?: ""
            val ctx = LocalContext.current
            UploadDetailScreen(
                fileName = fileName,
                onBackClick = { navController.popBackStack() },
                onDeleteFile = { pendingUploadUri = null; navController.popBackStack() },
                onUploadClick = { title, genreId ->
                    val uri = pendingUploadUri ?: return@UploadDetailScreen
                    scope.launch {
                        try {
                            val inputStream = ctx.contentResolver.openInputStream(uri)
                            val bytes = inputStream?.readBytes() ?: return@launch
                            val mimeType = ctx.contentResolver.getType(uri) ?: "audio/mpeg"
                            val songPart = MultipartBody.Part.createFormData(
                                "song", fileName,
                                bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                            )
                            val titleBody = title.toRequestBody("text/plain".toMediaTypeOrNull())
                            val res = RetrofitClient.api.uploadSong(
                                token = TokenManager.getBearerToken(),
                                song = songPart,
                                cover = null,
                                title = titleBody,
                                genreId = genreId?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
                            )
                            if (res.isSuccessful) {
                                navController.navigate("upload_complete") {
                                    popUpTo("upload_initial") { inclusive = true }
                                }
                            } else {
                                Toast.makeText(ctx, "업로드 실패: ${res.code()}", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(ctx, "오류: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }

        composable("settings") {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onLoginRequired = { navController.navigate("login") },
                onLogout = {
                    navController.navigate("main") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable("upload_complete") {
            UploadCompleteScreen(
                onGoMainClick = {
                    navController.navigate("main") {
                        popUpTo("main") { inclusive = true }
                    }
                },
                onGoMusicClick = {
                    navController.navigate("player/0?autoPlay=true") {
                        popUpTo("main") { inclusive = false }
                    }
                }
            )
        }
    }
}
