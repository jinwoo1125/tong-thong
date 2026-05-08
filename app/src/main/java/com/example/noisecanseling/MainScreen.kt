package com.example.noisecanseling

import android.content.Intent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

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
    val champagneGold = Color(0xFFFFDA79)
    val glowOrange = Color(0xFFFF9F43)

    // 별 위치를 고정 (recomposition 때마다 바뀌지 않도록)
    val stars = remember {
        List(40) {
            Triple(Random.nextFloat(), Random.nextFloat(), Random.nextFloat())
        }
    }

    val transition = rememberInfiniteTransition(label = "splash")

    // 스윕 라이트 (shimmer 이동)
    val shimmerOffset by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing)),
        label = "shimmer"
    )

    // 별 깜빡임 (전체 알파)
    val starPulse by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "starPulse"
    )

    // 로고 glow pulse (네온 골드 flicker)
    val glowAlpha by transition.animateFloat(
        initialValue = 0.0f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "glow"
    )

    // 네온 골드 flicker (빠른 깜빡임 레이어)
    val flickerAlpha by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            tween(110, easing = LinearEasing),
            RepeatMode.Reverse
        ),
        label = "flicker"
    )

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // 배경 별 + shimmer 레이어
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 움직이는 shimmer 띠
            val shimmerX = w * shimmerOffset
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        champagneGold.copy(alpha = 0.06f),
                        champagneGold.copy(alpha = 0.15f),
                        glowOrange.copy(alpha = 0.08f),
                        champagneGold.copy(alpha = 0.06f),
                        Color.Transparent
                    ),
                    start = Offset(shimmerX - 200f, 0f),
                    end = Offset(shimmerX + 200f, h)
                )
            )

            // 반짝이는 별들
            stars.forEachIndexed { i, (rx, ry, phase) ->
                val x = rx * w
                val y = ry * h
                val alpha = ((starPulse + phase) % 1f).let { if (it > 0.5f) 1f - it else it } * 2f
                val radius = 1.5f + phase * 3f
                val starColor = if (phase > 0.6f) glowOrange else champagneGold

                // 별 중심 빛
                drawCircle(
                    color = starColor.copy(alpha = alpha * 0.9f),
                    radius = radius,
                    center = Offset(x, y)
                )
                // 십자 빛줄기
                val arm = radius * 3f
                drawLine(starColor.copy(alpha = alpha * 0.4f), Offset(x - arm, y), Offset(x + arm, y), strokeWidth = 1f)
                drawLine(starColor.copy(alpha = alpha * 0.4f), Offset(x, y - arm), Offset(x, y + arm), strokeWidth = 1f)
            }
        }

        // 로고 glow 후광 — 네온 샴페인 골드 다층 글로우
        Box(
            modifier = Modifier
                .size(480.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            glowOrange.copy(alpha = glowAlpha * 0.20f * flickerAlpha),
                            champagneGold.copy(alpha = glowAlpha * 0.50f * flickerAlpha),
                            champagneGold.copy(alpha = glowAlpha * 0.25f * flickerAlpha),
                            champagneGold.copy(alpha = glowAlpha * 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )
        // 내부 코어 글로우 (타이트한 오렌지 핫스팟)
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            glowOrange.copy(alpha = glowAlpha * 0.40f * flickerAlpha),
                            champagneGold.copy(alpha = glowAlpha * 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )

        // 로고 이미지
        Image(
            painter = painterResource(id = R.drawable.noise_logo_start),
            contentDescription = "Noise",
            modifier = Modifier
                .size(300.dp)
                .graphicsLayer { alpha = 0.95f + glowAlpha * 0.05f },
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
    onNavigateToSettings: () -> Unit = {},
    onNavigateToArtist: (String) -> Unit = {},
    onNavigateToGenre: (String) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(MainTab.Home) }

    Scaffold(
        containerColor = AppBg,
        bottomBar = {
            Column {
                MiniPlayer(onPlayerClick = { id -> onNavigateToPlayer(id, false) })
                NoiseBottomNavigation(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(AppBg)
        ) {
            when (selectedTab) {
                MainTab.Home -> HomeScreen(onSongClick = { onNavigateToPlayer(it, true) }, onNavigateToArtist = onNavigateToArtist)
                MainTab.Chart -> ChartScreen(
                    onSongClick = { onNavigateToPlayer(it, true) },
                    onGenreClick = onNavigateToGenre,
                    onNavigateToArtist = onNavigateToArtist
                )
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
                    onSettingsClick = onNavigateToSettings,
                    onArtistClick = onNavigateToArtist
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
                    selectedIconColor = Color(0xFFFFDA79),
                    selectedTextColor = Color(0xFFFFDA79),
                    unselectedIconColor = AppSubText,
                    unselectedTextColor = AppSubText,
                    indicatorColor = Color(0xFF2A2200)
                )
            )
        }
    }
}

// ── 미니 플레이어 ────────────────────────────
@Composable
fun MiniPlayer(onPlayerClick: (Int) -> Unit) {
    val songId = AppPlayer.currentSongId.intValue
    val isPlaying = AppPlayer.isPlaying.value
    val title = AppPlayer.currentTitle.value
    val artist = AppPlayer.currentArtist.value
    val context = LocalContext.current
    val exoPlayer = remember { (context.applicationContext as App).exoPlayer }

    if (songId == -1) return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .background(Color(0xE8121620))
            .clickable { onPlayerClick(songId) }
    ) {
        // 재생 진행 바 (하단)
        val progress = AppPlayer.sliderPosition.floatValue
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(2.dp)
                .background(AppPrimary)
                .align(Alignment.BottomStart)
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 앨범 커버 (미니)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(coverBrush(songId.coerceAtLeast(1))),
                contentAlignment = Alignment.Center
            ) {
                Text("?", color = AppText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(Modifier.width(12.dp))

            // 제목 + 아티스트
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    color = AppText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (artist.isNotBlank()) {
                    Text(artist, color = AppSubText, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            // 이전 곡
            IconButton(onClick = { exoPlayer.seekBack() }) {
                Icon(Icons.Rounded.SkipPrevious, contentDescription = "이전", tint = AppText, modifier = Modifier.size(24.dp))
            }

            // 재생/일시정지
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AppPrimary)
                    .clickable { AppPlayer.isPlaying.value = !isPlaying },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(4.dp))

            // 다음 곡
            IconButton(onClick = { exoPlayer.seekForward() }) {
                Icon(Icons.Rounded.SkipNext, contentDescription = "다음", tint = AppText, modifier = Modifier.size(24.dp))
            }
        }
    }
}
