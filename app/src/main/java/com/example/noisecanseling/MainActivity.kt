package com.example.noisecanseling

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        enableEdgeToEdge()
        // 앱 시작 시 저장된 토큰으로 로그인 상태 복원
        isLoggedIn = TokenManager.isLoggedIn()

        // Android 13+ 알림 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

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
                onNavigateToUpload = { navController.navigate("upload") },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToArtist = { name -> navController.navigate("artist/${name}") },
                onNavigateToGenre = { name -> navController.navigate("genre/${name}") }
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
                onPlaylistClick = { navController.navigate("playlist") },
                onNextTrack = { nextId ->
                    navController.navigate("player/$nextId?autoPlay=true") {
                        popUpTo("player/$index?autoPlay=$autoPlay") { inclusive = true }
                    }
                },
                onPrevTrack = { prevId ->
                    if (prevId >= 0) navController.navigate("player/$prevId?autoPlay=true") {
                        popUpTo("player/$index?autoPlay=$autoPlay") { inclusive = true }
                    }
                }
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

        composable("upload") {
            val ctx = LocalContext.current
            UploadScreen(
                onBackClick = { navController.popBackStack() },
                onUploadClick = { data ->
                    scope.launch {
                        try {
                            val plain = "text/plain".toMediaTypeOrNull()

                            // 음원 파일
                            val audioStream = ctx.contentResolver.openInputStream(data.audioUri)
                            val audioBytes = audioStream?.readBytes() ?: return@launch
                            val audioMime = ctx.contentResolver.getType(data.audioUri) ?: "audio/mpeg"
                            val songPart = MultipartBody.Part.createFormData(
                                "song", data.audioFileName,
                                audioBytes.toRequestBody(audioMime.toMediaTypeOrNull())
                            )

                            // 표지 이미지 (선택)
                            val coverPart = data.coverUri?.let { uri ->
                                val bytes = ctx.contentResolver.openInputStream(uri)?.readBytes()
                                val mime = ctx.contentResolver.getType(uri) ?: "image/jpeg"
                                bytes?.let {
                                    MultipartBody.Part.createFormData(
                                        "cover", "cover.jpg",
                                        it.toRequestBody(mime.toMediaTypeOrNull())
                                    )
                                }
                            }

                            val res = RetrofitClient.api.uploadSong(
                                token = TokenManager.getBearerToken(),
                                song = songPart,
                                cover = coverPart,
                                title = data.title.toRequestBody(plain),
                                genreId = data.genreId?.toString()?.toRequestBody(plain),
                                description = data.description.ifBlank { null }?.toRequestBody(plain),
                                lyrics = data.lyrics.ifBlank { null }?.toRequestBody(plain)
                            )
                            if (res.isSuccessful) {
                                chartRefreshKey++  // 차트 자동 갱신 트리거
                                navController.navigate("upload_complete") {
                                    popUpTo("upload") { inclusive = true }
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

        composable(
            route = "artist/{artistName}",
            arguments = listOf(navArgument("artistName") { type = NavType.StringType })
        ) { backStackEntry ->
            val artistName = backStackEntry.arguments?.getString("artistName") ?: ""
            ArtistDetailScreen(
                artistName = artistName,
                onBackClick = { navController.popBackStack() },
                onSongClick = { songId -> navController.navigate("player/$songId?autoPlay=true") }
            )
        }

        composable(
            route = "genre/{genreName}",
            arguments = listOf(navArgument("genreName") { type = NavType.StringType })
        ) { backStackEntry ->
            val genreName = backStackEntry.arguments?.getString("genreName") ?: ""
            GenreDetailScreen(
                genreName = genreName,
                onBackClick = { navController.popBackStack() },
                onSongClick = { songId -> navController.navigate("player/$songId?autoPlay=true") }
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
