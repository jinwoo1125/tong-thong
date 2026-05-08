package com.example.noisecanseling

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noisecanseling.network.AddCommentRequest
import com.example.noisecanseling.network.CommentResponse
import com.example.noisecanseling.network.RetrofitClient
import com.example.noisecanseling.network.SaveRequest
import com.example.noisecanseling.network.SongResponse
import com.example.noisecanseling.network.TokenManager
import kotlinx.coroutines.launch

private val chartGenreIdMap = mapOf("팝" to 1, "힙합" to 2, "R&B" to 3, "록" to 4, "재즈" to 5, "클래식" to 6, "일렉트로닉" to 7, "인디" to 8, "발라드" to 9, "트로트" to 10, "OST" to 11, "기타" to 12)

data class GenreCard(val name: String, val id: Int?, val gradientStart: Color, val gradientEnd: Color, val emoji: String, val imageRes: Int? = null)

private val genreCards = listOf(
    GenreCard("전체",    null, Color(0xFF2C2C3E), Color(0xFF1A1A2E), "🎵", R.drawable.genre_all),
    GenreCard("팝",      1,    Color(0xFFFF6B9D), Color(0xFFFF8E53), "🎤", R.drawable.genre_pop),
    GenreCard("힙합",   2,    Color(0xFF2D1B69), Color(0xFF11998E), "🎧", R.drawable.genre_hiphop),
    GenreCard("R&B",    3,    Color(0xFF6A0572), Color(0xFFAD1457), "🎶", R.drawable.genre_rnb),
    GenreCard("록",      4,    Color(0xFF870000), Color(0xFF190A05), "🎸", R.drawable.genre_rock),
    GenreCard("재즈",   5,    Color(0xFF654321), Color(0xFFD4A017), "🎷", R.drawable.genre_jazz),
    GenreCard("클래식", 6,    Color(0xFF5C4033), Color(0xFFC8A96E), "🎻", R.drawable.genre_classic),
    GenreCard("일렉트로닉", 7, Color(0xFF0052D4), Color(0xFF00C3FF), "🎛️", R.drawable.genre_electronic),
    GenreCard("인디",   8,    Color(0xFF134E5E), Color(0xFF71B280), "🌿", R.drawable.genre_indie),
    GenreCard("발라드", 9,    Color(0xFF1A237E), Color(0xFF90CAF9), "🌙", R.drawable.genre_ballad),
    GenreCard("트로트", 10,   Color(0xFF7B1FA2), Color(0xFFE91E63), "🎺", R.drawable.genre_trot),
    GenreCard("OST",    11,   Color(0xFF1B5E20), Color(0xFFA5D6A7), "🎬", R.drawable.genre_ost),
    GenreCard("기타",   12,   Color(0xFF37474F), Color(0xFF78909C), "🎼", R.drawable.genre_etc),
)

@Composable
fun ChartScreen(onSongClick: (Int) -> Unit = {}, onGenreClick: (String) -> Unit = {}, onNavigateToArtist: (String) -> Unit = {}) {
    var selectedMain by remember { mutableStateOf("기간별") }
    var selectedPeriod by remember { mutableStateOf("일간") }
    var sortBy by remember { mutableStateOf("조회수") }
    var sortAsc by remember { mutableStateOf(false) }
    val mainFilters = listOf("기간별", "장르")
    val periods = listOf("일간", "주간", "월간", "년간")
    val sortOptions = listOf("조회수", "좋아요", "댓글")

    var songs by remember { mutableStateOf<List<SongResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    val periodParam = when (selectedPeriod) {
        "주간" -> "weekly"; "월간" -> "monthly"; "년간" -> "yearly"; else -> "daily"
    }

    // Retrofit 기반 기간별 차트 (chartRefreshKey 업로드 트리거 포함)
    LaunchedEffect(selectedMain, selectedPeriod, chartRefreshKey) {
        if (selectedMain != "기간별") return@LaunchedEffect
        isLoading = true
        try {
            val res = RetrofitClient.api.getChart(period = periodParam)
            if (res.isSuccessful) songs = res.body() ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }


    val displayedSongs = if (selectedMain == "기간별") {
        val sorted = when (sortBy) {
            "좋아요" -> songs.sortedByDescending { it.like_count }
            "댓글"  -> songs.sortedByDescending { it.comment_count }
            else    -> songs.sortedByDescending { it.play_count }
        }
        if (sortAsc) sorted.reversed() else sorted
    } else emptyList()

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(AppBg).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                NoiseMark()
                Text(
                    "NOISE CHART",
                    color = AppText,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = BebasNeueFontFamily,
                    letterSpacing = 3.sp
                )
            }
        }

        // 기간별 / 장르별 텍스트 탭
        item { NoiseTextTabs(mainFilters, selectedMain) { selectedMain = it } }


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
            // 장르 카드 그리드 (2열) - 탭하면 새 화면으로 이동
            item {
                GenreCardGrid(selectedGenre = "", onSelect = { genreName ->
                    if (genreName != "전체") onGenreClick(genreName)
                    else onGenreClick("전체")
                })
            }
        }

        if (selectedMain == "기간별") {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("정렬:", color = AppSubText, fontSize = 12.sp)
                    Spacer(Modifier.width(8.dp))
                    sortOptions.forEach { option ->
                        val isSelected = sortBy == option
                        Button(
                            onClick = {
                                if (isSelected) sortAsc = !sortAsc
                                else { sortBy = option; sortAsc = false }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) AppPrimary else AppSurface),
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(option, color = if (isSelected) AppBg else AppText, fontSize = 13.sp)
                            if (isSelected) {
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    imageVector = if (sortAsc) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = AppBg,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
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
                        onPlayClick = { onSongClick(song.id) },
                        onRowClick = { onSongClick(song.id) },
                        onNavigateToArtist = onNavigateToArtist
                    )
                }
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
                SongDetailRow("제목", song.title ?: "-")
                SongDetailRow("업로더", song.uploader ?: "-")
                SongDetailRow("장르", song.genre ?: "-")
                SongDetailRow("조회수", "${song.play_count}회")
                SongDetailRow("좋아요", "${song.like_count}개")
                SongDetailRow("댓글", "${song.comment_count}개")
                SongDetailRow("업로드", song.created_at?.take(10) ?: "-")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("닫기", color = AppPrimary) }
        }
    )
}

@Composable
fun AlbumInfoDialog(song: SongResponse, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppSurface,
        title = { Text("앨범 정보", color = AppText, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    MiniCover(song.id)
                }
                Spacer(Modifier.height(4.dp))
                SongDetailRow("앨범", song.title?.let { "Noise - $it" } ?: "-")
                SongDetailRow("아티스트", song.uploader ?: "-")
                SongDetailRow("장르", song.genre ?: "-")
                SongDetailRow("발매일", song.created_at?.take(10) ?: "-")
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
fun ApiChartSongRow(
    rank: Int,
    song: SongResponse,
    onPlayClick: () -> Unit = {},
    onRowClick: () -> Unit = {},
    onNavigateToArtist: (String) -> Unit = {}
) {
    var showDetail by remember { mutableStateOf(false) }
    var showAlbumInfo by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showSave by remember { mutableStateOf(false) }
    var showComment by remember { mutableStateOf(false) }
    var avgScore by remember { mutableStateOf<String?>(null) }
    var myScore by remember { mutableIntStateOf(0) }
    var isLiked by remember { mutableStateOf(LikedSongsManager.isLiked(song.id)) }
    val isArtistOwner = isLoggedIn && song.uploader == com.example.noisecanseling.network.TokenManager.getNickname()

    LaunchedEffect(song.id) {
        try {
            val res = RetrofitClient.api.getRating(
                token = com.example.noisecanseling.network.TokenManager.getBearerToken(),
                id = song.id
            )
            if (res.isSuccessful) {
                avgScore = res.body()?.avg_score
                myScore = res.body()?.my_score ?: 0
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            val likeRes = RetrofitClient.api.getLikeStatus(
                token = com.example.noisecanseling.network.TokenManager.getBearerToken(),
                id = song.id
            )
            if (likeRes.isSuccessful) isLiked = likeRes.body()?.liked ?: false
        } catch (e: Exception) { /* ignore */ }
    }

    Box(modifier = Modifier.clickable { onRowClick() }) {
        SongRowFrame {
            Text("$rank", color = AppText, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp))
            MiniCover(song.id)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(blindTitle(song.id), color = AppText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${song.play_count}회", color = AppSubText, fontSize = 12.sp)
                    if (avgScore != null) {
                        Text("  ★ ${"%.1f".format(avgScore!!.toFloatOrNull() ?: 0f)}", color = AppPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            IconButton(
                onClick = {
                    isLiked = !isLiked
                    if (isLiked) LikedSongsManager.like(song) else LikedSongsManager.unlike(song.id)
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "좋아요",
                    tint = if (isLiked) Color(0xFFFF0000) else AppSubText,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(onClick = onPlayClick, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.PlayArrow, contentDescription = "재생", tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.MoreVert, contentDescription = "더보기", tint = AppSubText, modifier = Modifier.size(20.dp))
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, containerColor = AppSurface) {
                    DropdownMenuItem(
                        text = { Text("곡 정보", color = AppText) },
                        leadingIcon = { Icon(Icons.Default.Info, null, tint = AppSubText) },
                        onClick = { showMenu = false; showDetail = true }
                    )
                    DropdownMenuItem(
                        text = { Text("앨범 정보", color = AppText) },
                        leadingIcon = { Icon(Icons.Default.Album, null, tint = AppSubText) },
                        onClick = { showMenu = false; showAlbumInfo = true }
                    )
                    DropdownMenuItem(
                        text = { Text("아티스트 채널", color = AppText) },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = AppSubText) },
                        onClick = { showMenu = false; song.uploader?.let { onNavigateToArtist(it) } }
                    )
                    DropdownMenuItem(
                        text = { Text("저장", color = AppText) },
                        leadingIcon = { Icon(Icons.Default.BookmarkAdd, null, tint = AppSubText) },
                        onClick = { showMenu = false; showSave = true }
                    )
                    DropdownMenuItem(
                        text = { Text("댓글", color = AppText) },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Send, null, tint = AppSubText) },
                        onClick = { showMenu = false; showComment = true }
                    )
                }
            }
        }
    }

    if (showDetail) {
        ApiSongDetailDialog(song = song, onDismiss = { showDetail = false })
    }
    if (showAlbumInfo) {
        AlbumInfoDialog(song = song, onDismiss = { showAlbumInfo = false })
    }
    if (showSave) {
        SaveDialog(songId = song.id, songTitle = song.title ?: blindTitle(song.id), onDismiss = { showSave = false })
    }
    if (showComment) {
        CommentDialog(songId = song.id, isRated = myScore > 0, isArtistOwner = isArtistOwner, onDismiss = { showComment = false })
    }
}

@Composable
fun SaveDialog(songId: Int, songTitle: String, onDismiss: () -> Unit) {
    var customTitle by remember { mutableStateOf(songTitle) }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppSurface,
        title = { Text("내 보관함에 저장", color = AppText, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("저장할 이름을 입력하세요", color = AppSubText, fontSize = 13.sp)
                OutlinedTextField(
                    value = customTitle,
                    onValueChange = { customTitle = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppPrimary,
                        unfocusedBorderColor = AppSubText,
                        focusedTextColor = AppText,
                        unfocusedTextColor = AppText,
                        cursorColor = AppPrimary
                    )
                )
                if (message.isNotEmpty()) {
                    Text(message, color = if (message.startsWith("저장")) AppGreen else Color(0xFFFF6B6B), fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (!isLoading) {
                        isLoading = true
                        scope.launch {
                            try {
                                val res = RetrofitClient.api.saveSong(
                                    token = TokenManager.getBearerToken(),
                                    id = songId,
                                    body = SaveRequest(customTitle.ifBlank { songTitle })
                                )
                                message = if (res.isSuccessful) "저장되었습니다!" else "저장 실패: ${res.code()}"
                                if (res.isSuccessful) kotlinx.coroutines.delay(800L).also { onDismiss() }
                            } catch (e: Exception) {
                                message = "오류: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                }
            ) {
                if (isLoading) CircularProgressIndicator(color = AppPrimary, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text("저장", color = AppPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소", color = AppSubText) }
        }
    )
}

@Composable
fun CommentDialog(songId: Int, isRated: Boolean = false, isArtistOwner: Boolean = false, onDismiss: () -> Unit) {
    val canView = isRated || isArtistOwner
    var comments by remember { mutableStateOf<List<CommentResponse>>(emptyList()) }
    var newComment by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSending by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(songId) {
        if (canView) {
            try {
                val res = RetrofitClient.api.getComments(songId)
                if (res.isSuccessful) comments = res.body() ?: emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        isLoading = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppSurface,
        title = {
            Text(
                if (canView) "댓글 (${comments.size})" else "비공개 처리",
                color = AppText,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (!canView) {
                    // 댓글 잠금 UI
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = AppSubText,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "평점을 남기면 다른 유저들의\n댓글을 확인할 수 있습니다",
                            color = AppSubText,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(Modifier.height(20.dp))
                        // 비활성화된 입력창 (블러 효과 대체)
                        Box {
                            OutlinedTextField(
                                value = "",
                                onValueChange = {},
                                placeholder = { Text("댓글을 입력하세요", color = AppSubText) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = false,
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledBorderColor = AppSubText.copy(alpha = 0.3f),
                                    disabledTextColor = AppSubText.copy(alpha = 0.3f)
                                )
                            )
                            // 반투명 오버레이로 블러 효과 표현
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(AppSurface.copy(alpha = 0.7f))
                            )
                        }
                    }
                } else {
                    if (isLoading) {
                        Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = AppPrimary, modifier = Modifier.size(24.dp))
                        }
                    } else if (comments.isEmpty()) {
                        Text("아직 댓글이 없어요. 첫 번째 댓글을 남겨보세요!", color = AppSubText, fontSize = 13.sp, modifier = Modifier.padding(vertical = 12.dp))
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 240.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(comments) { comment ->
                                Column {
                                    Text(comment.nickname, color = AppPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Text(comment.content, color = AppText, fontSize = 13.sp)
                                    Text(comment.created_at.take(10), color = AppSubText, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = newComment,
                            onValueChange = { newComment = it },
                            placeholder = { Text("댓글을 입력하세요", color = AppSubText) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppPrimary,
                                unfocusedBorderColor = AppSubText,
                                focusedTextColor = AppText,
                                unfocusedTextColor = AppText,
                                cursorColor = AppPrimary
                            )
                        )
                        IconButton(
                            onClick = {
                                if (newComment.isNotBlank() && !isSending) {
                                    isSending = true
                                    scope.launch {
                                        try {
                                            val res = RetrofitClient.api.addComment(
                                                token = TokenManager.getBearerToken(),
                                                id = songId,
                                                body = AddCommentRequest(newComment)
                                            )
                                            if (res.isSuccessful) {
                                                newComment = ""
                                                val refreshed = RetrofitClient.api.getComments(songId)
                                                if (refreshed.isSuccessful) comments = refreshed.body() ?: comments
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        } finally {
                                            isSending = false
                                        }
                                    }
                                }
                            }
                        ) {
                            if (isSending) CircularProgressIndicator(color = AppPrimary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            else Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "전송", tint = AppPrimary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("닫기", color = AppPrimary) }
        }
    )
}

@Composable
fun GenreCardGrid(selectedGenre: String, onSelect: (String) -> Unit) {
    val rows = genreCards.chunked(2)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { rowCards ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                rowCards.forEach { genre ->
                    val isSelected = selectedGenre == genre.name
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(90.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .then(
                                if (isSelected) Modifier.border(
                                    width = 2.dp,
                                    color = AppPrimary,
                                    shape = RoundedCornerShape(16.dp)
                                ) else Modifier
                            )
                            .clickable { onSelect(genre.name) },
                        contentAlignment = Alignment.Center
                    ) {
                        // 배경: 이미지 우선, 없으면 그라디언트
                        if (genre.imageRes != null) {
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(genre.imageRes),
                                contentDescription = null,
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier.matchParentSize()
                            )
                            // 어두운 오버레이
                            Box(modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.45f)))
                        } else {
                            Box(
                                modifier = Modifier.matchParentSize().background(
                                    Brush.linearGradient(
                                        listOf(
                                            genre.gradientStart.copy(alpha = if (isSelected) 1f else 0.85f),
                                            genre.gradientEnd.copy(alpha = if (isSelected) 1f else 0.85f)
                                        )
                                    )
                                )
                            )
                        }
                        // 텍스트/아이콘
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(genre.emoji, fontSize = 24.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                genre.name,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                        if (isSelected) {
                            Box(
                                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                                    .size(18.dp).clip(CircleShape).background(AppPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✓", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                if (rowCards.size == 1) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun NoiseTextTabs(items: List<String>, selected: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = item == selected
            Box(
                modifier = Modifier
                    .clickable { onSelect(item) }
                    .padding(horizontal = 24.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        item,
                        color = if (isSelected) AppText else AppSubText,
                        fontSize = 15.sp,
                        fontFamily = InterFontFamily,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(2.dp)
                            .background(if (isSelected) AppPrimary else Color.Transparent)
                    )
                }
            }
            if (index < items.lastIndex) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(20.dp)
                        .align(Alignment.CenterVertically)
                        .background(AppSubText.copy(alpha = 0.3f))
                )
            }
        }
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

