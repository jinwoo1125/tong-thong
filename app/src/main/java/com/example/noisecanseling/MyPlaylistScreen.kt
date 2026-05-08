package com.example.noisecanseling

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val PL_BG = Color(0xFF0E1117)
private val PL_SURFACE = Color(0xFF171B24)
private val PL_PRIMARY = Color(0xFFFFC857)
private val PL_TEXT = Color.White
private val PL_SUBTEXT = Color(0xFF9BA3B4)

data class PlaylistSong(
    val id: Int,
    val title: String,
    val artist: String,
)

val playlistDummyData = songList.mapIndexed { index, song ->
    PlaylistSong(id = index, title = song.title, artist = song.artist)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPlaylistScreen(
    onBackClick: () -> Unit = {},
    onPlayClick: (songIndex: Int) -> Unit = {}
) {
    var isEditMode by remember { mutableStateOf(false) }
    var selectedItems by remember { mutableStateOf(setOf<Int>()) }
    var songs by remember { mutableStateOf(playlistDummyData) }
    var showDropdown by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    fun exitEditMode() {
        isEditMode = false
        selectedItems = emptySet()
    }

    // 삭제 확인 다이얼로그
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = PL_SURFACE,
            title = { Text("삭제하시겠습니까?", color = PL_TEXT) },
            text = { Text("선택한 ${selectedItems.size}곡을 플레이리스트에서 삭제합니다.", color = PL_SUBTEXT) },
            confirmButton = {
                TextButton(onClick = {
                    songs = songs.filter { it.id !in selectedItems }
                    selectedItems = emptySet(); showDeleteDialog = false
                    if (songs.isEmpty()) exitEditMode()
                }) { Text("확인", color = Color(0xFFFF6B6B)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("취소", color = PL_SUBTEXT) }
            }
        )
    }

    Scaffold(
        containerColor = PL_BG,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PL_BG),
                title = {
                    Text(text = if (isEditMode) "${selectedItems.size}곡 선택됨" else "내 플레이리스트",
                        fontWeight = FontWeight.SemiBold, color = PL_TEXT)
                },
                navigationIcon = {
                    IconButton(onClick = { if (isEditMode) exitEditMode() else onBackClick() }) {
                        Icon(if (isEditMode) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null, tint = PL_TEXT)
                    }
                },
                actions = {
                    if (isEditMode) {
                        TextButton(onClick = {
                            selectedItems = if (selectedItems.size == songs.size) emptySet() else songs.map { it.id }.toSet()
                        }) { Text(if (selectedItems.size == songs.size) "전체 해제" else "전체 선택", color = PL_PRIMARY) }
                    } else {
                        Box {
                            IconButton(onClick = { showDropdown = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "더보기", tint = PL_TEXT)
                            }
                            DropdownMenu(expanded = showDropdown, onDismissRequest = { showDropdown = false },
                                containerColor = PL_SURFACE) {
                                DropdownMenuItem(text = { Text("편집 (순서/삭제)", color = PL_TEXT) },
                                    onClick = { showDropdown = false; isEditMode = true },
                                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = PL_SUBTEXT) })
                                DropdownMenuItem(text = { Text("전체 재생", color = PL_TEXT) },
                                    onClick = { showDropdown = false; onPlayClick(0) },
                                    leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = PL_SUBTEXT) })
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            AnimatedVisibility(visible = selectedItems.isNotEmpty(),
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()) {
                Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp).clip(RoundedCornerShape(20.dp)),
                    color = PL_SURFACE, shadowElevation = 8.dp) {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "${selectedItems.size}곡 선택됨", fontSize = 15.sp, fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f), color = PL_TEXT)
                        OutlinedButton(onClick = {
                            val firstSelected = songs.indexOfFirst { it.id in selectedItems }
                            onPlayClick(if (firstSelected >= 0) firstSelected else 0)
                        }, shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PL_PRIMARY)) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("재생")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = { showDeleteDialog = true }, shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B6B))) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("삭제")
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(PL_BG).padding(innerPadding),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            itemsIndexed(songs) { index, song ->
                PlaylistItem(
                    song = song,
                    isEditMode = isEditMode,
                    isSelected = song.id in selectedItems,
                    onCheckedChange = { checked ->
                        selectedItems = if (checked)
                            selectedItems + song.id
                        else
                            selectedItems - song.id
                    },
                    onSelectToggle = {
                        selectedItems = if (song.id in selectedItems)
                            selectedItems - song.id
                        else
                            selectedItems + song.id
                    },
                    onClick = { onPlayClick(song.id) }
                )
                if (index < songs.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun PlaylistItem(
    song: PlaylistSong,
    isEditMode: Boolean,
    isSelected: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onSelectToggle: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
            .background(if (isSelected) PL_PRIMARY.copy(alpha = 0.1f) else PL_BG)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = isSelected, onCheckedChange = { onSelectToggle() },
            modifier = Modifier.padding(end = 4.dp),
            colors = CheckboxDefaults.colors(checkedColor = PL_PRIMARY, uncheckedColor = PL_SUBTEXT, checkmarkColor = PL_BG))

        Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(8.dp)).background(PL_SURFACE),
            contentAlignment = Alignment.Center) {
            Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(26.dp), tint = PL_PRIMARY.copy(alpha = 0.5f))
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = song.title, fontSize = 15.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, color = PL_TEXT)
            Spacer(modifier = Modifier.height(3.dp))
            Text(text = song.artist, fontSize = 13.sp, color = PL_SUBTEXT, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }

        AnimatedVisibility(visible = isEditMode) {
            Icon(Icons.Rounded.DragHandle, contentDescription = "순서 변경",
                modifier = Modifier.padding(start = 8.dp).size(24.dp), tint = PL_SUBTEXT)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "내 플레이리스트 - 기본")
@Composable
fun MyPlaylistScreenPreview() {
    MaterialTheme { MyPlaylistScreen() }
}
