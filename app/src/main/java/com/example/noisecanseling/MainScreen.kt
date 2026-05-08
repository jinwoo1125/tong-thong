package com.example.noisecanseling

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── 바텀 탭 ─────────────────────────────────
enum class MainTab(val title: String, val icon: ImageVector) {
    Upload("올리기", Icons.Default.Add),
    Library("라이브러리", Icons.Default.Book),
    Home("홈", Icons.Default.Home),
    Chart("차트", Icons.Default.BarChart),
    MyPage("마이", Icons.Default.Person)
}

// ── 스플래시 화면 ────────────────────────────
@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.noise_logo_start),
            contentDescription = "Noise Cancelling logo",
            modifier = Modifier.size(300.dp),
            contentScale = ContentScale.Fit
        )
    }
}

// ── 메인 앱 (바텀 내비 + 각 탭) ──────────────
@Composable
fun MainApp(
    onLoginRequired: () -> Unit = {},
    onNavigateToPlayer: (Int, Boolean) -> Unit = { _, _ -> },
    onNavigateToPlaylist: () -> Unit = {},
    onNavigateToUpload: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(MainTab.Home) }

    Scaffold(
        containerColor = AppBg,
        bottomBar = {
            NoiseBottomNavigation(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(AppBg)
        ) {
            when (selectedTab) {
                MainTab.Home -> HomeScreen(onSongClick = { onNavigateToPlayer(it, true) })
                MainTab.Chart -> ChartScreen(onSongClick = { onNavigateToPlayer(it, true) })
                MainTab.Library -> LibraryScreen(
                    onSongClick = { onNavigateToPlayer(it, true) },
                    onPlaylistClick = onNavigateToPlaylist
                )
                MainTab.Upload -> {
                    LaunchedEffect(Unit) {
                        onNavigateToUpload()
                        selectedTab = MainTab.Home
                    }
                }
                MainTab.MyPage -> MyPageScreen(
                    onSongClick = { onNavigateToPlayer(it, true) },
                    onLoginClick = onLoginRequired,
                    onSettingsClick = onNavigateToSettings
                )
            }
        }
    }
}

// ── 바텀 네비게이션 바 ────────────────────────
@Composable
fun NoiseBottomNavigation(selectedTab: MainTab, onTabSelected: (MainTab) -> Unit) {
    NavigationBar(containerColor = AppSurface) {
        MainTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                icon = { Icon(tab.icon, contentDescription = tab.title) },
                label = { Text(tab.title, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AppPrimary,
                    selectedTextColor = AppPrimary,
                    unselectedIconColor = AppSubText,
                    unselectedTextColor = AppSubText,
                    indicatorColor = AppSurfaceHi
                )
            )
        }
    }
}
