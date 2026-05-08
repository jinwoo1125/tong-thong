package com.example.noisecanseling

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noisecanseling.network.RetrofitClient
import com.example.noisecanseling.network.SongResponse
import com.example.noisecanseling.network.TokenManager

private val genreIdMap = mapOf(
    "팝" to 1, "힙합" to 2, "R&B" to 3, "록" to 4, "재즈" to 5,
    "클래식" to 6, "일렉트로닉" to 7, "인디" to 8, "발라드" to 9,
    "트로트" to 10, "OST" to 11, "기타" to 12
)

private val genreEmojiMap = mapOf(
    "팝" to "🎤", "힙합" to "🎧", "R&B" to "🎶", "록" to "🎸", "재즈" to "🎷",
    "클래식" to "🎻", "일렉트로닉" to "🎛️", "인디" to "🌿", "발라드" to "🌙",
    "트로트" to "🎺", "OST" to "🎬", "기타" to "🎼", "전체" to "🎵"
)

private val genreImageMap = mapOf<String, Int>(
    "힙합" to R.drawable.genre_hiphop,
    "R&B"  to R.drawable.genre_rnb,
    "팝"   to R.drawable.genre_pop,
    "재즈" to R.drawable.genre_jazz,
    "인디"   to R.drawable.genre_indie,
    "발라드" to R.drawable.genre_ballad,
    "트로트" to R.drawable.genre_trot,
    "OST"    to R.drawable.genre_ost,
    "록"       to R.drawable.genre_rock,
    "일렉트로닉" to R.drawable.genre_electronic,
    "클래식" to R.drawable.genre_classic,
    "기타"   to R.drawable.genre_etc,
    "전체"   to R.drawable.genre_all
)

private val genreGradientMap = mapOf(
    "팝"       to Pair(Color(0xFFFF6B9D), Color(0xFFFF8E53)),
    "힙합"     to Pair(Color(0xFF2D1B69), Color(0xFF11998E)),
    "R&B"      to Pair(Color(0xFF6A0572), Color(0xFFAD1457)),
    "록"       to Pair(Color(0xFF870000), Color(0xFF190A05)),
    "재즈"     to Pair(Color(0xFF654321), Color(0xFFD4A017)),
    "클래식"   to Pair(Color(0xFF5C4033), Color(0xFFC8A96E)),
    "일렉트로닉" to Pair(Color(0xFF0052D4), Color(0xFF00C3FF)),
    "인디"     to Pair(Color(0xFF134E5E), Color(0xFF71B280)),
    "발라드"   to Pair(Color(0xFF1A237E), Color(0xFF90CAF9)),
    "트로트"   to Pair(Color(0xFF7B1FA2), Color(0xFFE91E63)),
    "OST"      to Pair(Color(0xFF1B5E20), Color(0xFFA5D6A7)),
    "기타"     to Pair(Color(0xFF37474F), Color(0xFF78909C)),
    "전체"     to Pair(Color(0xFF2C2C3E), Color(0xFF1A1A2E)),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenreDetailScreen(
    genreName: String,
    onBackClick: () -> Unit = {},
    onSongClick: (Int) -> Unit = {}
) {
    var songs by remember { mutableStateOf<List<SongResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var sortBy by remember { mutableStateOf("조회수") }
    var sortAsc by remember { mutableStateOf(false) }
    val sortOptions = listOf("조회수", "좋아요", "댓글")

    val gradient = genreGradientMap[genreName] ?: Pair(Color(0xFF2C2C3E), Color(0xFF1A1A2E))
    val emoji = genreEmojiMap[genreName] ?: "🎵"
    val genreId = genreIdMap[genreName]
    val headerImageRes = genreImageMap[genreName]

    LaunchedEffect(genreName) {
        isLoading = true
        try {
            val res = RetrofitClient.api.getSongs(genreId = genreId, limit = 50)
            if (res.isSuccessful) songs = res.body() ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    val displayedSongs = run {
        val sorted = when (sortBy) {
            "좋아요" -> songs.sortedByDescending { it.like_count }
            "댓글"   -> songs.sortedByDescending { it.comment_count }
            else     -> songs.sortedByDescending { it.play_count }
        }
        if (sortAsc) sorted.reversed() else sorted
    }

    Scaffold(
        containerColor = AppBg,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            NoiseMark()
                            Text(
                                text = genreName.uppercase(),
                                color = AppText,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = BebasNeueFontFamily,
                                letterSpacing = 2.sp
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기", tint = AppText)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
                if (isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().height(2.dp),
                        color = AppPrimary,
                        trackColor = Color.Transparent
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // 헤더 배너
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (headerImageRes != null) {
                        Image(
                            painter = painterResource(headerImageRes),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize()
                        )
                        // 어두운 그라디언트 오버레이
                        Box(
                            modifier = Modifier.matchParentSize().background(
                                Brush.verticalGradient(
                                    listOf(Color.Black.copy(alpha = 0.2f), Color.Black.copy(alpha = 0.65f))
                                )
                            )
                        )
                    } else {
                        Box(
                            modifier = Modifier.matchParentSize().background(
                                Brush.linearGradient(listOf(gradient.first, gradient.second))
                            )
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (headerImageRes == null) Text(emoji, fontSize = 52.sp)
                        Spacer(Modifier.height(if (headerImageRes == null) 8.dp else 0.dp))
                        Text(
                            genreName,
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            "${songs.size}곡",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // 전체재생 / 셔플 버튼
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { if (displayedSongs.isNotEmpty()) onSongClick(displayedSongs.first().id) },
                        colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("전체 재생", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { if (displayedSongs.isNotEmpty()) onSongClick(displayedSongs.random().id) },
                        colors = ButtonDefaults.buttonColors(containerColor = AppSurfaceHi),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Shuffle, contentDescription = null, tint = AppText, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("셔플 재생", color = AppText)
                    }
                }
            }

            // 정렬 버튼
            item {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("정렬:", color = AppSubText, fontSize = 12.sp)
                    Spacer(Modifier.width(8.dp))
                    sortOptions.forEach { option ->
                        val isSelected = sortBy == option
                        Button(
                            onClick = {
                                if (isSelected) sortAsc = !sortAsc
                                else { sortBy = option; sortAsc = false }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) AppPrimary else AppSurface
                            ),
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(option, color = if (isSelected) AppBg else AppText, fontSize = 13.sp)
                            if (isSelected) {
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    if (sortAsc) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = AppBg,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 곡 목록
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AppPrimary)
                    }
                }
            } else if (displayedSongs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("등록된 곡이 없습니다", color = AppSubText, fontSize = 14.sp)
                    }
                }
            } else {
                itemsIndexed(displayedSongs) { index, song ->
                    var showDetail by remember { mutableStateOf(false) }
                    var showSave by remember { mutableStateOf(false) }
                    var showComment by remember { mutableStateOf(false) }
                    var myScore by remember { mutableStateOf(0) }
                    var isLiked by remember { mutableStateOf(LikedSongsManager.isLiked(song.id)) }

                    LaunchedEffect(song.id) {
                        try {
                            val res = RetrofitClient.api.getRating(TokenManager.getBearerToken(), song.id)
                            if (res.isSuccessful) myScore = res.body()?.my_score ?: 0
                        } catch (e: Exception) { e.printStackTrace() }
                        try {
                            val likeRes = RetrofitClient.api.getLikeStatus(TokenManager.getBearerToken(), song.id)
                            if (likeRes.isSuccessful) isLiked = likeRes.body()?.liked ?: false
                        } catch (e: Exception) { /* ignore */ }
                    }

                    Box(modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 10.dp)) {
                        SongRowFrame {
                            Text(
                                "${index + 1}",
                                color = AppPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(28.dp)
                            )
                            MiniCover(song.id)
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    blindTitle(song.id),
                                    color = AppText,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "${song.play_count}회",
                                    color = AppSubText,
                                    fontSize = 11.sp
                                )
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
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(onClick = { onSongClick(song.id) }, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "재생", tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                            IconButton(onClick = { showDetail = true }, modifier = Modifier.size(36.dp)) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = "더보기",
                                    tint = AppSubText,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    if (showDetail) ApiSongDetailDialog(song = song, onDismiss = { showDetail = false })
                    if (showSave) SaveDialog(songId = song.id, songTitle = song.title ?: blindTitle(song.id), onDismiss = { showSave = false })
                    if (showComment) {
                        val isOwner = isLoggedIn && song.uploader == com.example.noisecanseling.network.TokenManager.getNickname()
                        CommentDialog(songId = song.id, isRated = myScore > 0, isArtistOwner = isOwner, onDismiss = { showComment = false })
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}
