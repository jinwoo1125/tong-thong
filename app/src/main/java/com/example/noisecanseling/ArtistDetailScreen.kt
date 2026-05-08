package com.example.noisecanseling

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noisecanseling.network.RetrofitClient
import com.example.noisecanseling.network.SongResponse
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(
    artistName: String,
    onBackClick: () -> Unit = {},
    onSongClick: (Int) -> Unit = {}
) {
    var songs by remember { mutableStateOf<List<SongResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val isFollowing = FollowManager.isFollowing(artistName)
    val scope = rememberCoroutineScope()

    // 아티스트 곡 목록 + 팔로우 상태 로드
    LaunchedEffect(artistName) {
        isLoading = true
        try {
            val res = RetrofitClient.api.getSongs(page = 1, limit = 50)
            if (res.isSuccessful) {
                songs = (res.body() ?: emptyList()).filter { it.uploader == artistName }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
        if (isLoggedIn) FollowManager.loadFollowStatus(artistName)
    }

    val totalPlays = songs.sumOf { it.play_count }
    val totalLikes = songs.sumOf { it.like_count }
    val totalComments = songs.sumOf { it.comment_count }

    Scaffold(
        containerColor = AppBg,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기", tint = AppText)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (isLoggedIn) scope.launch { FollowManager.toggle(artistName) }
                    }) {
                        Icon(
                            if (isFollowing) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "팔로우",
                            tint = if (isFollowing) AppPrimary else AppSubText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBg)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // 히어로 헤더
            item {
                ArtistHeroSection(
                    artistName = artistName,
                    isFollowing = isFollowing,
                    onFollowClick = {
                        if (isLoggedIn) scope.launch { FollowManager.toggle(artistName) }
                    }
                )
            }

            // 통계 카드
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatChip(modifier = Modifier.weight(1f), icon = Icons.Default.PlayArrow, label = "재생", value = formatCount(totalPlays))
                    StatChip(modifier = Modifier.weight(1f), icon = Icons.Default.Favorite, label = "좋아요", value = formatCount(totalLikes))
                    StatChip(modifier = Modifier.weight(1f), icon = Icons.Default.Comment, label = "댓글", value = formatCount(totalComments))
                    StatChip(modifier = Modifier.weight(1f), icon = Icons.Default.MusicNote, label = "곡 수", value = "${songs.size}")
                }
            }

            // 갤러리 (LP 커버들)
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text("앨범 갤러리", color = AppText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                }
                if (songs.isEmpty() && !isLoading) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(5) { i ->
                            ArtistGalleryItem(seed = i + 1)
                        }
                    }
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(songs.take(6)) { song ->
                            ArtistGalleryItem(seed = song.id)
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // 작사/작곡 정보
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text("작사 · 작곡", color = AppText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AppSurface),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            ArtistInfoRow(label = "작사", value = artistName)
                            HorizontalDivider(color = AppBg.copy(alpha = 0.5f))
                            ArtistInfoRow(label = "작곡", value = artistName)
                            HorizontalDivider(color = AppBg.copy(alpha = 0.5f))
                            ArtistInfoRow(label = "편곡", value = "본인 전담")
                            HorizontalDivider(color = AppBg.copy(alpha = 0.5f))
                            ArtistInfoRow(label = "소속사", value = "독립 아티스트")
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                }
            }

            // 곡 목록
            item {
                Text(
                    "업로드한 곡",
                    color = AppText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(10.dp))
            }

            if (isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AppPrimary)
                    }
                }
            } else if (songs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("아직 업로드한 곡이 없어요", color = AppSubText, fontSize = 14.sp)
                    }
                }
            } else {
                itemsIndexed(songs) { index, song ->
                    Box(modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 10.dp)) {
                        ArtistSongRow(
                            rank = index + 1,
                            song = song,
                            onPlayClick = { onSongClick(song.id) }
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun ArtistHeroSection(artistName: String, isFollowing: Boolean, onFollowClick: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "hero")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing)),
        label = "heroRot"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(AppSurface, AppBg)
                )
            )
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 아티스트 아바타 (LP 스타일)
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .rotate(rotation)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(listOf(AppPrimary.copy(alpha = 0.9f), AppPrimary.copy(alpha = 0.3f), AppBg))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(AppBg), contentAlignment = Alignment.Center) {
                    Text(
                        artistName.take(1).uppercase(),
                        color = AppPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(artistName, color = AppText, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Text("블라인드 크리에이터", color = AppSubText, fontSize = 13.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                "음악으로 감정을 전달하는 아티스트",
                color = AppSubText,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(6.dp))

            // 데뷔일 & 팔로워
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = AppSubText, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("2024.01.01 데뷔", color = AppSubText, fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.People, contentDescription = null, tint = AppSubText, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("팔로워 1,248", color = AppSubText, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onFollowClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFollowing) AppSurface else AppPrimary
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier.width(140.dp)
            ) {
                Icon(
                    if (isFollowing) Icons.Default.Check else Icons.Default.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (isFollowing) AppSubText else AppBg
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    if (isFollowing) "팔로잉" else "팔로우",
                    color = if (isFollowing) AppSubText else AppBg,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun StatChip(modifier: Modifier = Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.height(4.dp))
            Text(value, color = AppText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(label, color = AppSubText, fontSize = 10.sp)
        }
    }
}

@Composable
fun ArtistGalleryItem(seed: Int) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(coverBrush(seed)),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.5f)))
    }
}

@Composable
fun ArtistSongRow(rank: Int, song: SongResponse, onPlayClick: () -> Unit = {}) {
    SongRowFrame {
        Text("$rank", color = AppPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp))
        MiniCover(song.id)
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(blindTitle(song.id), color = AppText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text("${song.play_count}회 재생 · ♥ ${song.like_count}", color = AppSubText, fontSize = 11.sp)
        }
        IconButton(onClick = onPlayClick) {
            Icon(Icons.Default.PlayArrow, contentDescription = "재생", tint = AppPrimary)
        }
    }
}

@Composable
fun ArtistInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = AppSubText, fontSize = 13.sp)
        Text(value, color = AppText, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

private fun formatCount(count: Int): String = when {
    count >= 1_000_000 -> "%.1fM".format(count / 1_000_000f)
    count >= 1_000 -> "%.1fK".format(count / 1_000f)
    else -> "$count"
}
