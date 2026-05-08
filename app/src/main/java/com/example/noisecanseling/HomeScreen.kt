package com.example.noisecanseling

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noisecanseling.network.RetrofitClient
import kotlinx.coroutines.launch
import kotlin.math.abs


// ── ChartSong 데이터 (차트/홈 표시용) ──────────
data class ChartSong(
    val id: Int,
    val title: String,
    val blindName: String,
    val genre: String,
    val mood: String,
    val rankChange: Int,
    val totalStreams: String,
    val dailyStreams: String
)

data class MoodPlaylist(
    val title: String,
    val moods: String,
    val songs: List<ChartSong>
)

val sampleChartSongs = listOf(
    ChartSong(1, "새벽의 파동", "N#트랙 91406", "R&B", "몽환적인, 부드러운", 3, "1,240,000", "48,500"),
    ChartSong(2, "푸른 잔향", "T#폰트 87054", "Indie", "잔잔한, 따뜻한", -1, "986,000", "31,880"),
    ChartSong(3, "Round Signal", "B#라운드 30218", "Dance", "경쾌한, 밝은", 8, "812,400", "29,100"),
    ChartSong(4, "Night Code", "M#블라인드 11870", "Hip-hop", "강한, 어두운", 2, "640,900", "22,940"),
    ChartSong(5, "소금빛 바다", "A#소리 44013", "Ballad", "먹먹한, 깊은", 0, "532,100", "18,450"),
    ChartSong(6, "Orbit Run", "Z#트랙 71904", "Rock", "거친, 시원한", 5, "488,700", "16,200")
)

val quickPlaylists = listOf(
    MoodPlaylist("드라이브", "청량한, 리듬감 있는, 밤공기", sampleChartSongs.take(3)),
    MoodPlaylist("작업 몰입", "차분한, 반복감 있는, 낮은 온도", sampleChartSongs.drop(1).take(3)),
    MoodPlaylist("새벽 감성", "몽환적인, 느린, 보컬 중심", sampleChartSongs.shuffled().take(3))
)

// ── 홈 화면 ────────────────────────────────────
@Composable
fun HomeScreen(onSongClick: (Int) -> Unit = {}, onNavigateToArtist: (String) -> Unit = {}) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(AppBg).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(18.dp))
            NoiseLogo()
        }
        item { UserHero() }
        item {
            SectionTitle("빠른 선곡")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                items(quickPlaylists) { playlist -> QuickPlaylistCard(playlist, onSongClick) }
            }
        }
        item {
            SectionTitle("실시간 음악 TOP 10")
            Spacer(modifier = Modifier.height(12.dp))
            val allTop10 = sampleChartSongs + sampleChartSongs.take(4)
            Top10FanCarousel(songs = allTop10, onSongClick = onSongClick)
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

// ── 공통 UI 컴포넌트 ──────────────────────────
@Composable
fun NoiseLogo() {
    // #10: 홈 LP 로고 무한 회전
    val infiniteTransition = rememberInfiniteTransition(label = "logo")
    val logoRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)),
        label = "logoRot"
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(id = R.drawable.vinyl_logo),
            contentDescription = "Noise",
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(46.dp).clip(CircleShape).rotate(logoRotation)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text("NOISE", color = AppText, fontSize = 24.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun NoiseMark() {
    val transition = rememberInfiniteTransition(label = "vinyl")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)),
        label = "vinyl_rotation"
    )
    Image(
        painter = painterResource(id = R.drawable.vinyl_logo),
        contentDescription = "Noise",
        contentScale = ContentScale.Crop,
        modifier = Modifier.size(46.dp).clip(CircleShape).rotate(rotation)
    )
}

// #3: 나머지 TOP 10 – 이름 없이 무한 자동 회전하는 LP
@Composable
fun AutoSpinCover(song: ChartSong, rotation: Float) {
    VinylCover(seed = song.id, rotation = rotation, size = 84)
}

@Composable
fun UserHero() {
    Card(colors = CardDefaults.cardColors(containerColor = AppSurface), shape = RoundedCornerShape(18.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Avatar()
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(globalNickname, color = AppText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("오늘은 드라이브 무드가 잘 맞아요", color = AppSubText, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun Avatar() {
    AvatarCircle(index = globalAvatarIndex, size = 58)
}

@Composable
fun QuickPlaylistCard(playlist: MoodPlaylist, onPlayClick: (Int) -> Unit = {}) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var savedToast by remember { mutableStateOf(false) }

    LaunchedEffect(savedToast) {
        if (savedToast) {
            kotlinx.coroutines.delay(1500L)
            savedToast = false
        }
    }

    Card(
        modifier = Modifier.width(230.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        shape = RoundedCornerShape(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(playlist.title, color = AppText, fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = InterFontFamily)
            Text(playlist.moods, color = AppSubText, fontSize = 12.sp, maxLines = 2, fontFamily = InterFontFamily)
            Spacer(modifier = Modifier.height(18.dp))
            Row {
                playlist.songs.forEach { song ->
                    MiniCover(song.id)
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            if (savedToast) {
                Text("라이브러리에 저장됨 ✓", color = AppPrimary, fontSize = 11.sp, fontFamily = InterFontFamily)
                Spacer(Modifier.height(4.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Play button
                IconButton(
                    onClick = {
                        if (!isLoading) {
                            isLoading = true
                            scope.launch {
                                try {
                                    val res = RetrofitClient.api.getSongs(page = 1, limit = 20)
                                    val songs = if (res.isSuccessful) res.body() else null
                                    val firstSong = songs?.randomOrNull()
                                    if (firstSong != null) onPlayClick(firstSong.id)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    },
                    modifier = Modifier.background(AppPrimary, RoundedCornerShape(0.dp))
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = AppBg, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = "재생", tint = AppBg)
                    }
                }
                // Save to playlist button
                val alreadySaved = PlaylistManager.hasThemePlaylist(playlist.title)
                IconButton(
                    onClick = {
                        PlaylistManager.syncThemePlaylist(playlist)
                        savedToast = true
                    },
                    modifier = Modifier.background(
                        if (alreadySaved) AppPrimary.copy(alpha = 0.25f) else AppSurfaceHi,
                        RoundedCornerShape(0.dp)
                    )
                ) {
                    Icon(
                        if (alreadySaved) Icons.Default.Bookmark else Icons.Default.BookmarkAdd,
                        contentDescription = "저장",
                        tint = if (alreadySaved) AppPrimary else AppText
                    )
                }
            }
        }
    }
}

@Composable
fun RotatingTopCover(song: ChartSong, rotation: Float, onClick: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(94.dp).clickable { onClick() }
    ) {
        VinylCover(seed = song.id, rotation = rotation, size = 84)
        Spacer(modifier = Modifier.height(8.dp))
        Text(song.blindName, color = AppText, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

private val vinylLabelColors = listOf(
    Color(0xFF64B5F6), // blue
    Color(0xFFFF8A65), // orange
    Color(0xFF81C784), // green
    Color(0xFFBA68C8), // purple
    Color(0xFFFFD54F), // yellow
    Color(0xFFE57373), // red
    Color(0xFF4DD0E1), // cyan
    Color(0xFFF06292), // pink
    Color(0xFFA5D6A7), // light green
    Color(0xFFFFB74D), // amber
)

@Composable
fun VinylCover(seed: Int, rotation: Float, size: Int = 84) {
    val context = LocalContext.current
    val labelColor = vinylLabelColors[((seed - 1).coerceAtLeast(0)) % vinylLabelColors.size]

    val vinylBitmap = remember {
        BitmapFactory.decodeResource(context.resources, R.drawable.vinyl_single).asImageBitmap()
    }

    Canvas(
        modifier = Modifier
            .size(size.dp)
            .rotate(rotation)
            .clip(CircleShape)
    ) {
        val w = this.size.width.toInt()
        val h = this.size.height.toInt()
        drawImage(image = vinylBitmap, dstSize = androidx.compose.ui.unit.IntSize(w, h))
        // center label overlay
        val radius = this.size.minDimension * 0.22f
        drawCircle(color = labelColor, radius = radius)
        drawCircle(color = Color.Black.copy(alpha = 0.6f), radius = radius * 0.25f)
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(title, color = AppText, fontSize = 21.sp, fontWeight = FontWeight.Bold)
}

// ── 공유 곡 행 컴포넌트 ───────────────────────
@Composable
fun SongRowFrame(content: @Composable RowScope.() -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = AppSurface), shape = RoundedCornerShape(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
fun MiniCover(seed: Int) {
    Box(
        modifier = Modifier.size(48.dp).background(coverBrush(seed)),
        contentAlignment = Alignment.Center
    ) {
        Text("?", color = AppText, fontWeight = FontWeight.Bold, fontSize = 20.sp)
    }
}

@Composable
fun RankChange(change: Int) {
    val text = when { change > 0 -> "+$change"; change < 0 -> "$change"; else -> "-" }
    val color = if (change >= 0) AppGreen else Color(0xFFFF6B6B)
    Text(text, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(34.dp))
}

@Composable
fun RowActions(
    onPlayClick: () -> Unit = {},
    onDetailClick: () -> Unit = {},
    onAlbumClick: () -> Unit = {},
    onArtistClick: () -> Unit = {},
    onSaveClick: () -> Unit = {},
    onCommentClick: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onPlayClick) {
            Icon(Icons.Default.PlayArrow, contentDescription = "재생", tint = Color.White)
        }
        Box {
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "메뉴", tint = AppText)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, containerColor = AppSurface) {
                DropdownMenuItem(text = { Text("곡 정보", color = AppText) }, leadingIcon = { Icon(Icons.Default.Info, null, tint = AppSubText) }, onClick = { expanded = false; onDetailClick() })
                DropdownMenuItem(text = { Text("앨범 정보", color = AppText) }, leadingIcon = { Icon(Icons.Default.Album, null, tint = AppSubText) }, onClick = { expanded = false; onAlbumClick() })
                DropdownMenuItem(text = { Text("아티스트 채널", color = AppText) }, leadingIcon = { Icon(Icons.Default.Person, null, tint = AppSubText) }, onClick = { expanded = false; onArtistClick() })
                DropdownMenuItem(text = { Text("저장", color = AppText) }, leadingIcon = { Icon(Icons.Default.BookmarkAdd, null, tint = AppSubText) }, onClick = { expanded = false; onSaveClick() })
                DropdownMenuItem(text = { Text("댓글", color = AppText) }, leadingIcon = { Icon(Icons.Default.Comment, null, tint = AppSubText) }, onClick = { expanded = false; onCommentClick() })
            }
        }
    }
}

fun coverBrush(seed: Int): Brush {
    val palettes = listOf(
        listOf(Color(0xFFFFC857), Color(0xFF58C4DD)),
        listOf(Color(0xFFFF6B6B), Color(0xFF7BD88F)),
        listOf(Color(0xFFB8E986), Color(0xFF4A90E2)),
        listOf(Color(0xFFFF9F1C), Color(0xFF2EC4B6))
    )
    return Brush.linearGradient(palettes[seed % palettes.size])
}

// ── 실시간 TOP 10 팬 카드 캐러셀 ──────────────
@Composable
fun Top10FanCarousel(songs: List<ChartSong>, onSongClick: (Int) -> Unit) {
    val pagerState = rememberPagerState(pageCount = { songs.size })

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        contentAlignment = Alignment.Center
    ) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 72.dp),
            pageSpacing = 8.dp,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val rawOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            val offset = rawOffset.coerceIn(-2f, 2f)
            val rotation = offset * (-18f)
            val scale = 1f - abs(offset) * 0.12f
            val elevationOffset = abs(offset) * 24f

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        rotationZ = rotation
                        scaleX = scale
                        scaleY = scale
                        translationY = elevationOffset
                        // 중앙 카드가 앞으로
                        shadowElevation = (1f - abs(offset)) * 24f
                    }
                    .fillMaxHeight()
                    .aspectRatio(0.68f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(coverBrush(songs[page].id))
                    .border(
                        width = if (abs(offset) < 0.3f) 2.dp else 0.dp,
                        brush = Brush.linearGradient(listOf(AppPrimary, Color.Transparent)),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onSongClick(songs[page].id) },
                contentAlignment = Alignment.Center
            ) {
                // 순위 번호 (배경)
                Text(
                    "${page + 1}",
                    color = Color.White.copy(alpha = 0.15f),
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 10.dp, bottom = 4.dp)
                )

                // 재생 버튼 원
                Box(
                    modifier = Modifier
                        .size(if (abs(offset) < 0.3f) 60.dp else 48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "재생",
                        tint = Color.White,
                        modifier = Modifier.size(if (abs(offset) < 0.3f) 34.dp else 26.dp)
                    )
                }

                // 곡 이름 (하단)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        blindTitle(songs[page].id),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // 페이지 인디케이터 (점)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 0.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(minOf(songs.size, 10)) { index ->
                val selected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .size(if (selected) 6.dp else 4.dp)
                        .clip(CircleShape)
                        .background(if (selected) AppPrimary else AppSubText.copy(alpha = 0.4f))
                )
            }
        }
    }
}
