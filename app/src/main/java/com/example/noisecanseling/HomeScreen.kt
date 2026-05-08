package com.example.noisecanseling

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


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
fun HomeScreen(onSongClick: (Int) -> Unit = {}) {
    val topListState = rememberLazyListState()

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
            // #3: 처음 3개는 스크롤 연동 회전, 나머지는 무한 자동 회전
            val autoTransition = rememberInfiniteTransition(label = "top10auto")
            val autoRotation by autoTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)),
                label = "autoRot"
            )
            val allTop10 = sampleChartSongs + sampleChartSongs.take(4)
            LazyRow(state = topListState, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                itemsIndexed(allTop10) { index, song ->
                    if (index < 3) {
                        val rotation = (topListState.firstVisibleItemScrollOffset / 4f) + song.id * 18f
                        RotatingTopCover(song = song, rotation = rotation, onClick = { onSongClick(song.id % songList.size) })
                    } else {
                        AutoSpinCover(song = song, rotation = autoRotation + index * 45f)
                    }
                }
            }
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
        Box(
            modifier = Modifier
                .size(46.dp)
                .rotate(logoRotation)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(AppPrimary, AppAccent))),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Noise", tint = AppBg)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text("NOISE", color = AppText, fontSize = 24.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun NoiseMark() {
    Box(
        modifier = Modifier.size(46.dp).clip(CircleShape)
            .background(Brush.linearGradient(listOf(AppPrimary, AppAccent))),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.PlayArrow, contentDescription = "Noise", tint = AppBg)
    }
}

// #3: 나머지 TOP 10 – 이름 없이 무한 자동 회전하는 LP
@Composable
fun AutoSpinCover(song: ChartSong, rotation: Float) {
    Box(
        modifier = Modifier
            .size(84.dp)
            .rotate(rotation)
            .clip(CircleShape)
            .background(coverBrush(song.id)),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.size(18.dp).clip(CircleShape).background(AppBg))
    }
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
    Card(
        modifier = Modifier.width(230.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(playlist.title, color = AppText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(playlist.moods, color = AppSubText, fontSize = 12.sp, maxLines = 2)
            Spacer(modifier = Modifier.height(18.dp))
            Row {
                playlist.songs.forEach { song ->
                    MiniCover(song.id)
                    Spacer(modifier = Modifier.width(6.dp))
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { onPlayClick(0) }, modifier = Modifier.background(AppPrimary, CircleShape)) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "재생", tint = AppBg)
                }
                IconButton(onClick = {}, modifier = Modifier.background(AppSurfaceHi, CircleShape)) {
                    Icon(Icons.Default.Bookmark, contentDescription = "저장", tint = AppText)
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
        Box(
            modifier = Modifier.size(84.dp).rotate(rotation).clip(CircleShape).background(coverBrush(song.id)),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.size(18.dp).clip(CircleShape).background(AppBg))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(song.blindName, color = AppText, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(coverBrush(seed)),
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
fun RowActions(onPlayClick: () -> Unit = {}, onDetailClick: () -> Unit = {}) {
    var expanded by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onPlayClick) {
            Icon(Icons.Default.PlayArrow, contentDescription = "재생", tint = AppPrimary)
        }
        Box {
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "메뉴", tint = AppText)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, containerColor = AppSurface) {
                DropdownMenuItem(text = { Text("저장", color = AppText) }, leadingIcon = { Icon(Icons.Default.Save, null, tint = AppSubText) }, onClick = { expanded = false })
                // #8: 곡정보 클릭 시 상세 다이얼로그 호출
                DropdownMenuItem(text = { Text("곡정보", color = AppText) }, leadingIcon = { Icon(Icons.Default.Info, null, tint = AppSubText) }, onClick = { expanded = false; onDetailClick() })
                DropdownMenuItem(text = { Text("댓글", color = AppText) }, leadingIcon = { Icon(Icons.Default.Comment, null, tint = AppSubText) }, onClick = { expanded = false })
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
