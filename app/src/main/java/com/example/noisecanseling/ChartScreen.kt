package com.example.noisecanseling

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noisecanseling.network.RetrofitClient
import com.example.noisecanseling.network.SongResponse
import kotlinx.coroutines.launch

// 장르 목록 & ID 매핑
private val chartGenreList = listOf("전체", "팝", "힙합", "R&B", "록", "재즈", "클래식", "일렉트로닉", "인디", "발라드", "트로트", "OST", "기타")
private val chartGenreIdMap = mapOf("팝" to 1, "힙합" to 2, "R&B" to 3, "록" to 4, "재즈" to 5, "클래식" to 6, "일렉트로닉" to 7, "인디" to 8, "발라드" to 9, "트로트" to 10, "OST" to 11, "기타" to 12)

@Composable
fun ChartScreen(onSongClick: (Int) -> Unit = {}) {
    var selectedMain by remember { mutableStateOf("기간별") }
    var selectedPeriod by remember { mutableStateOf("일간") }
    var selectedGenre by remember { mutableStateOf("전체") }  // #5: 장르 선택
    var sortBy by remember { mutableStateOf("조회수") }         // #7: 정렬 기준
    val mainFilters = listOf("기간별", "장르별")
    val periods = listOf("일간", "주간", "월간", "년간")
    val sortOptions = listOf("조회수", "좋아요", "댓글")

    var songs by remember { mutableStateOf<List<SongResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    val periodParam = when (selectedPeriod) {
        "주간" -> "weekly"; "월간" -> "monthly"; "년간" -> "yearly"; else -> "daily"
    }

    // #5: 탭/기간/장르 변경 시 API 재호출
    LaunchedEffect(selectedMain, selectedPeriod, selectedGenre) {
        isLoading = true
        try {
            if (selectedMain == "기간별") {
                val res = RetrofitClient.api.getChart(period = periodParam)
                if (res.isSuccessful) songs = res.body() ?: emptyList()
            } else {
                val genreId = chartGenreIdMap[selectedGenre]
                val res = RetrofitClient.api.getSongs(genreId = genreId)
                if (res.isSuccessful) songs = res.body() ?: emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    // #7: 클라이언트 정렬
    val displayedSongs = when (sortBy) {
        "좋아요" -> songs.sortedByDescending { it.like_count }
        "댓글"  -> songs.sortedByDescending { it.comment_count }
        else    -> songs
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(AppBg).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                NoiseMark()
                Text("Noise Chart", color = AppText, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text("기간별 · 장르별", color = AppSubText, fontSize = 12.sp)
            }
        }

        // 기간별 / 장르별 탭
        item { FilterChips(mainFilters, selectedMain) { selectedMain = it; selectedGenre = "전체" } }

        if (selectedMain == "기간별") {
            item { FilterChips(periods, selectedPeriod) { selectedPeriod = it } }
            // #4: 전체재생 / 셔플재생 동작 구현
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ActionButton("전체 재생", Icons.Default.PlayArrow) {
                        if (displayedSongs.isNotEmpty()) onSongClick(displayedSongs.first().id)
                    }
                    ActionButton("셔플 재생", Icons.Default.Shuffle) {
                        if (displayedSongs.isNotEmpty()) onSongClick(displayedSongs.random().id)
                    }
                }
            }
        } else {
            // #5: 장르별 탭 – 장르 칩 목록
            item { FilterChips(chartGenreList, selectedGenre) { selectedGenre = it } }
        }

        // #7: 정렬 옵션 (조회수 / 좋아요 / 댓글)
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("정렬:", color = AppSubText, fontSize = 12.sp)
                Spacer(Modifier.width(8.dp))
                FilterChips(sortOptions, sortBy) { sortBy = it }
            }
        }

        if (isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppPrimary)
                }
            }
        } else {
            itemsIndexed(displayedSongs) { index, song ->
                ApiChartSongRow(
                    rank = index + 1,
                    song = song,
                    onPlayClick = { onSongClick(song.id) }
                )
            }
        }
    }
}

@Composable
fun ChartSongRow(rank: Int, song: ChartSong, onPlayClick: () -> Unit = {}) {
    SongRowFrame {
        Text("$rank", color = AppText, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp))
        MiniCover(song.id)
        Spacer(modifier = Modifier.width(10.dp))
        RankChange(song.rankChange)
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(song.blindName, color = AppText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("${song.genre} · ${song.mood}", color = AppSubText, fontSize = 12.sp, maxLines = 1)
        }
        RowActions(onPlayClick = onPlayClick)
    }
}

// #8: 곡 상세정보 다이얼로그
@Composable
fun ApiSongDetailDialog(song: SongResponse, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppSurface,
        title = { Text("곡 상세 정보", color = AppText, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SongDetailRow("제목", song.title)
                SongDetailRow("업로더", song.uploader)
                SongDetailRow("장르", song.genre ?: "-")
                SongDetailRow("조회수", "${song.play_count}회")
                SongDetailRow("좋아요", "${song.like_count}개")
                SongDetailRow("댓글", "${song.comment_count}개")
                SongDetailRow("업로드", song.created_at.take(10))
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("닫기", color = AppPrimary) }
        }
    )
}

@Composable
private fun SongDetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(label, color = AppSubText, fontSize = 13.sp, modifier = Modifier.width(56.dp))
        Text(value, color = AppText, fontSize = 13.sp)
    }
}

@Composable
fun ApiChartSongRow(rank: Int, song: SongResponse, onPlayClick: () -> Unit = {}) {
    var showDetail by remember { mutableStateOf(false) }
    SongRowFrame {
        Text("$rank", color = AppText, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp))
        MiniCover(song.id)
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(blindTitle(song.id), color = AppText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("${blindArtist(song.id)} · ${song.play_count}회 재생", color = AppSubText, fontSize = 12.sp, maxLines = 1)
        }
        RowActions(onPlayClick = onPlayClick, onDetailClick = { showDetail = true })
    }
    if (showDetail) {
        ApiSongDetailDialog(song = song, onDismiss = { showDetail = false })
    }
}

@Composable
fun FilterChips(items: List<String>, selected: String, onSelect: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
        items.forEach { item ->
            Button(
                onClick = { onSelect(item) },
                colors = ButtonDefaults.buttonColors(containerColor = if (item == selected) AppPrimary else AppSurface),
                shape = RoundedCornerShape(50)
            ) {
                Text(item, color = if (item == selected) AppBg else AppText)
            }
        }
    }
}

@Composable
fun ActionButton(text: String, icon: ImageVector, onClick: () -> Unit = {}) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = AppSurfaceHi),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(icon, contentDescription = text, tint = AppText, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, color = AppText)
    }
}
