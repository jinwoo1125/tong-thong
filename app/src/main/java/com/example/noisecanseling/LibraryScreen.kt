package com.example.noisecanseling

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LibraryScreen(onSongClick: (Int) -> Unit = {}, onPlaylistClick: () -> Unit = {}) {
    var selectedPlaylist by remember { mutableStateOf<UserPlaylist?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showLikedSongs by remember { mutableStateOf(false) }

    if (showAddDialog) {
        AddPlaylistDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name -> PlaylistManager.addPlaylist(name); showAddDialog = false }
        )
    }

    if (selectedPlaylist != null) {
        PlaylistDetailView(
            playlist = selectedPlaylist!!,
            onBack = { selectedPlaylist = null },
            onSongClick = onSongClick
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(AppBg).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                NoiseMark()
                Text(
                    "NOISE LIBRARY",
                    color = AppText,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = BebasNeueFontFamily,
                    letterSpacing = 3.sp
                )
            }
        }

        // 플레이리스트 헤더 + 추가 버튼
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "내 플레이리스트",
                    color = AppText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = InterFontFamily,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.size(36.dp).background(AppSurfaceHi)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "플레이리스트 추가", tint = AppPrimary, modifier = Modifier.size(20.dp))
                }
            }
        }

        // 플레이리스트 가로 스크롤 (Quick Pick 스타일)
        item {
            if (PlaylistManager.playlists.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(AppSurface)
                        .clickable { showAddDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = AppSubText, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.height(4.dp))
                        Text("+ 플레이리스트 만들기", color = AppSubText, fontSize = 13.sp, fontFamily = InterFontFamily)
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PlaylistManager.playlists.forEach { playlist ->
                        QuickStylePlaylistCard(
                            playlist = playlist,
                            onClick = { selectedPlaylist = playlist }
                        )
                    }
                }
            }
        }

        // Noise Like 섹션
        item { Spacer(Modifier.height(4.dp)) }
        item {
            LibraryFolder(
                title = "Noise Like",
                subtitle = "좋아요 한 음악 ${LikedSongsManager.likedSongIds.size}곡",
                icon = Icons.Default.Favorite,
                trailingIcon = if (showLikedSongs) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                onClick = { showLikedSongs = !showLikedSongs }
            )
        }

        if (showLikedSongs) {
            val likedSongs = LikedSongsManager.getLikedSongs()
            if (likedSongs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "좋아요한 곡이 없습니다\n곡을 들으며 ♥ 버튼을 눌러보세요",
                            color = AppSubText,
                            fontSize = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontFamily = InterFontFamily
                        )
                    }
                }
            } else {
                items(likedSongs, key = { it.id }) { song ->
                    LikedSongRow(song = song, onPlayClick = { onSongClick(song.id) })
                }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
fun PlaylistDetailView(
    playlist: UserPlaylist,
    onBack: () -> Unit,
    onSongClick: (Int) -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize().background(AppBg)) {
        // 헤더
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로", tint = AppText)
            }
            Text(
                playlist.name,
                color = AppText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = InterFontFamily,
                modifier = Modifier.weight(1f)
            )
        }

        // 전체/셔플 재생 버튼
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    val first = playlist.songs.firstOrNull()
                    if (first != null) {
                        val idx = songList.indexOfFirst { it.title == first.title }.coerceAtLeast(0)
                        onSongClick(idx)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = AppBg, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("전체 재생", color = AppBg, fontWeight = FontWeight.Bold, fontFamily = InterFontFamily)
            }
            Button(
                onClick = {
                    val random = playlist.songs.randomOrNull()
                    if (random != null) {
                        val idx = songList.indexOfFirst { it.title == random.title }.coerceAtLeast(0)
                        onSongClick(idx)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AppSurfaceHi),
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Shuffle, contentDescription = null, tint = AppText, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("셔플 재생", color = AppText, fontFamily = InterFontFamily)
            }
        }

        Text(
            "${playlist.songs.size}곡",
            color = AppSubText,
            fontSize = 13.sp,
            fontFamily = InterFontFamily,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(8.dp))

        if (playlist.songs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("곡이 없습니다", color = AppSubText, fontSize = 14.sp, fontFamily = InterFontFamily)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(playlist.songs) { index, song ->
                    val seed = kotlin.math.abs(song.title.hashCode())
                    SongRowFrame {
                        Text(
                            "${index + 1}",
                            color = AppPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(28.dp)
                        )
                        MiniCover(seed)
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(song.title, color = AppText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, fontFamily = InterFontFamily)
                            Text(song.artist, color = AppSubText, fontSize = 12.sp, fontFamily = InterFontFamily)
                        }
                        IconButton(
                            onClick = {
                                val idx = songList.indexOfFirst { it.title == song.title }.coerceAtLeast(0)
                                onSongClick(idx)
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "재생", tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                    }
                }
                item { Spacer(Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
fun AddPlaylistDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppSurface,
        title = { Text("새 플레이리스트", color = AppText, fontWeight = FontWeight.Bold, fontFamily = InterFontFamily) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("플레이리스트 이름") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppPrimary,
                    unfocusedBorderColor = AppSubText,
                    focusedLabelColor = AppPrimary,
                    unfocusedLabelColor = AppSubText,
                    cursorColor = AppPrimary,
                    focusedTextColor = AppText,
                    unfocusedTextColor = AppText
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onAdd(name) }) {
                Text("추가", color = AppPrimary, fontWeight = FontWeight.Bold, fontFamily = InterFontFamily)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = AppSubText, fontFamily = InterFontFamily)
            }
        }
    )
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
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(34.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = AppText, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = InterFontFamily)
                Text(subtitle, color = AppSubText, fontSize = 13.sp, fontFamily = InterFontFamily)
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
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Book, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(34.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(title, color = AppText, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = InterFontFamily)
                Text(subtitle, color = AppSubText, fontSize = 13.sp, fontFamily = InterFontFamily)
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
            Text(song.blindName, color = AppText, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = InterFontFamily)
            Text(song.mood, color = AppSubText, fontSize = 12.sp, fontFamily = InterFontFamily)
        }
        IconButton(onClick = onPlayClick) {
            Icon(Icons.Default.PlayArrow, contentDescription = "재생", tint = Color.White, modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
fun LikedSongRow(song: com.example.noisecanseling.network.SongResponse, onPlayClick: () -> Unit = {}) {
    SongRowFrame {
        MiniCover(song.id)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                blindTitle(song.id),
                color = AppText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = InterFontFamily
            )
            Text(
                "${song.play_count}회 재생",
                color = AppSubText,
                fontSize = 12.sp,
                fontFamily = InterFontFamily
            )
        }
        Icon(
            Icons.Default.Favorite,
            contentDescription = null,
            tint = Color(0xFFFF0000),
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(4.dp))
        IconButton(onClick = onPlayClick) {
            Icon(Icons.Default.PlayArrow, contentDescription = "재생", tint = Color.White, modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
fun QuickStylePlaylistCard(playlist: UserPlaylist, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.width(180.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        shape = RoundedCornerShape(0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Mini cover previews
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (playlist.songs.isEmpty()) {
                    Box(
                        modifier = Modifier.size(48.dp).background(AppSurfaceHi),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.MusicNote, contentDescription = null, tint = AppSubText, modifier = Modifier.size(24.dp))
                    }
                } else {
                    playlist.songs.take(3).forEach { song ->
                        MiniCover(kotlin.math.abs(song.title.hashCode()))
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                playlist.name,
                color = AppText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = InterFontFamily,
                maxLines = 1
            )
            Text(
                "${playlist.songs.size}곡",
                color = AppSubText,
                fontSize = 12.sp,
                fontFamily = InterFontFamily
            )
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(AppPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "재생", tint = AppBg, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun PlaylistCard(playlist: UserPlaylist, onClick: () -> Unit = {}) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(52.dp).background(AppSurfaceHi),
                contentAlignment = Alignment.Center
            ) {
                if (playlist.songs.isEmpty()) {
                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = AppSubText, modifier = Modifier.size(28.dp))
                } else {
                    val covers = playlist.songs.take(4)
                    if (covers.size < 4) {
                        MiniCover(kotlin.math.abs(covers.first().title.hashCode()))
                    } else {
                        Column {
                            Row {
                                covers.take(2).forEach { song ->
                                    val seed = kotlin.math.abs(song.title.hashCode())
                                    Box(
                                        modifier = Modifier.size(26.dp).background(coverBrush(seed)),
                                        contentAlignment = Alignment.Center
                                    ) { Text("?", color = AppText, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                                }
                            }
                            Row {
                                covers.drop(2).forEach { song ->
                                    val seed = kotlin.math.abs(song.title.hashCode())
                                    Box(
                                        modifier = Modifier.size(26.dp).background(coverBrush(seed)),
                                        contentAlignment = Alignment.Center
                                    ) { Text("?", color = AppText, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(playlist.name, color = AppText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, fontFamily = InterFontFamily)
                Text("${playlist.songs.size}곡", color = AppSubText, fontSize = 12.sp, fontFamily = InterFontFamily)
            }
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
    }
}
