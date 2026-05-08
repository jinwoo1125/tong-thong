package com.example.noisecanseling

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LibraryScreen(onSongClick: (Int) -> Unit = {}, onPlaylistClick: () -> Unit = {}) {
    // #2: 좋아요 탭 – 플레이리스트가 아닌 별도 섹션으로 펼치기
    var showLikedSongs by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(AppBg).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                NoiseMark()
                Text("Noise Library", color = AppText, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }

        // 플레이리스트 폴더 → 플레이리스트 화면 이동 (기존 유지)
        item { LibraryFolder("라이브러리 이름", "저장한 플레이리스트 12개", onClick = onPlaylistClick) }

        // #2: 좋아요 탭 → 플레이리스트가 아닌 좋아요 목록 인라인 펼치기
        item {
            LibraryFolder(
                title = "Noise like",
                subtitle = "좋아요 한 음악 ${LikedSongsManager.likedSongIds.size}곡",
                icon = Icons.Default.Favorite,
                trailingIcon = if (showLikedSongs) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                onClick = { showLikedSongs = !showLikedSongs }
            )
        }

        if (showLikedSongs) {
            if (LikedSongsManager.likedSongIds.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("좋아요한 곡이 없습니다", color = AppSubText, fontSize = 14.sp)
                    }
                }
            } else {
                val likedSongs = sampleChartSongs.filter { LikedSongsManager.isLiked(it.id) }
                items(likedSongs) { song ->
                    CompactSongRow(song, onPlayClick = { onSongClick(song.id % songList.size) })
                }
            }
        }

        // #6: 저장된 음악 목록 + 셔플/전체 재생 버튼
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { if (sampleChartSongs.isNotEmpty()) onSongClick(sampleChartSongs.first().id % songList.size) },
                    colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "전체 재생", tint = AppBg, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("전체 재생", color = AppBg, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { if (sampleChartSongs.isNotEmpty()) onSongClick(sampleChartSongs.random().id % songList.size) },
                    colors = ButtonDefaults.buttonColors(containerColor = AppSurfaceHi),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Shuffle, contentDescription = "셔플 재생", tint = AppText, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("셔플 재생", color = AppText)
                }
            }
        }

        items(sampleChartSongs.take(3)) { song ->
            CompactSongRow(song, onPlayClick = { onSongClick(song.id % songList.size) })
        }
    }
}

@Composable
fun LibraryFolder(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.Book,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit = {}
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(34.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = AppText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = AppSubText, fontSize = 13.sp)
            }
            if (trailingIcon != null) {
                Icon(trailingIcon, contentDescription = null, tint = AppSubText, modifier = Modifier.size(22.dp))
            }
        }
    }
}

@Composable
fun LibraryFolder(title: String, subtitle: String, onClick: () -> Unit = {}) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Book, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(34.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(title, color = AppText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = AppSubText, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun CompactSongRow(song: ChartSong, onPlayClick: () -> Unit = {}) {
    SongRowFrame {
        MiniCover(song.id)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(song.blindName, color = AppText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(song.mood, color = AppSubText, fontSize = 12.sp)
        }
        IconButton(onClick = onPlayClick) {
            Icon(Icons.Default.PlayArrow, contentDescription = "재생", tint = AppPrimary, modifier = Modifier.size(28.dp))
        }
    }
}
