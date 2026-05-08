package com.example.noisecanseling

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import coil.compose.AsyncImage
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import com.example.noisecanseling.network.RetrofitClient
import com.example.noisecanseling.network.SongResponse
import com.example.noisecanseling.network.TokenManager
import kotlinx.coroutines.launch

private val NC_BG      = Color(0xFF0E1117)
private val NC_SURFACE = Color(0xFF171B24)
private val NC_PRIMARY = Color(0xFFFFDA79)
private val NC_ACCENT  = Color(0xFF58C4DD)
private val NC_TEXT    = Color.White
private val NC_SUBTEXT = Color(0xFF9BA3B4)

// ── 곡 데이터 ────────────────────────────────────────────────────────
data class Song(
    val title: String,
    val artist: String,
    val durationSeconds: Int,
    val lyrics: List<String>
)

val songList = listOf(
    Song(
        title = "기다리고 있어",
        artist = "아티스트 A",
        durationSeconds = 225,
        lyrics = listOf(
            "눈을 감으면 떠오르는",
            "그 날의 기억들이",
            "아직도 선명하게 남아",
            "내 마음을 흔들어",
            "",
            "시간이 지나도 변하지 않는",
            "너와 함께했던 순간들",
            "바람처럼 스쳐 지나가도",
            "잊혀지지 않는 너",
            "",
            "그리워서 또 그리워서",
            "오늘도 이 노래를 불러",
            "네가 없는 이 자리에",
            "멈춰버린 시간처럼",
            "",
            "언제쯤이면 괜찮아질까",
            "이 마음이 식어갈까",
            "아직도 난 여기 서서",
            "너를 기다리고 있어",
        )
    ),
    Song(
        title = "봄이 오면",
        artist = "아티스트 B",
        durationSeconds = 198,
        lyrics = listOf(
            "차가운 바람이 멈추고",
            "따뜻한 햇살이 내려와",
            "긴 겨울이 끝났다는 걸",
            "꽃잎이 말해줘",
            "",
            "봄이 오면 생각나는",
            "그 골목길 벚꽃 아래",
            "너와 걷던 그 길 위에",
            "아직 향기가 남아",
            "",
            "다시 한번 그 계절로",
            "돌아갈 수 있다면",
            "네 손을 꼭 잡고서",
            "이번엔 놓지 않을게",
            "",
            "봄이 오면 또 그리워져",
            "매년 이맘때면 떠올라",
            "피고 지는 꽃처럼",
            "우리도 다시 피어날까",
        )
    ),
    Song(
        title = "밤하늘 아래",
        artist = "아티스트 C",
        durationSeconds = 242,
        lyrics = listOf(
            "별이 쏟아지는 밤",
            "너와 나란히 앉아",
            "아무 말 없이도",
            "충분했던 그 시간",
            "",
            "저 달이 기억할까",
            "우리가 나눈 이야기",
            "세상이 잠든 시간에",
            "빛나던 너의 눈빛",
            "",
            "밤이 깊어갈수록",
            "더 선명해지는 얼굴",
            "꿈인지 현실인지",
            "경계가 흐려져",
            "",
            "이 밤이 끝나지 않길",
            "영원히 여기 있길",
            "밤하늘 아래 우리",
            "이대로 멈춰버려",
        )
    ),
    Song(
        title = "이별 연습",
        artist = "아티스트 D",
        durationSeconds = 213,
        lyrics = listOf(
            "헤어지는 연습을 해",
            "매일 밤 혼자서",
            "괜찮다고 말하는 법",
            "웃으며 보내는 법",
            "",
            "사실은 아직 준비 안 됐어",
            "네가 없는 하루가",
            "얼마나 길고 긴지",
            "너는 알고 있니",
            "",
            "이별이 두려운 게 아냐",
            "너를 잃는 게 두려워",
            "내 일상 속 네가 빠지면",
            "남는 게 없을 것 같아",
            "",
            "그래도 보내줄게",
            "네가 원한다면",
            "이별 연습 다 끝났어",
            "이제 진짜 안녕",
        )
    ),
)

data class Comment(val nickname: String, val content: String, var likeCount: Int = 0)

val sampleComments = listOf(
    Comment("익명1", "진짜 명곡이다 ㅠㅠ"),
    Comment("익명2", "이 노래 들으면 항상 눈물남"),
    Comment("익명3", "멜로디가 너무 좋아요"),
    Comment("익명4", "반복재생 100번째"),
    Comment("익명5", "가사가 진짜 위로가 됨"),
)

// ── 가사 인덱스 계산 ─────────────────────────────────────────────────
private fun currentLyricIndex(currentSeconds: Int, totalSeconds: Int, lyricsCount: Int): Int {
    if (lyricsCount == 0 || totalSeconds == 0) return 0
    val progress = currentSeconds.toFloat() / totalSeconds.toFloat()
    return (progress * lyricsCount).toInt().coerceIn(0, lyricsCount - 1)
}

// ── 메인 플레이어 ─────────────────────────────────────────────────────
@UnstableApi
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    onBackClick: () -> Unit = {},
    onPlaylistClick: () -> Unit = {},
    onNextTrack: ((Int) -> Unit)? = null,
    onPrevTrack: ((Int) -> Unit)? = null,
    initialSongIndex: Int = 0,
    autoPlay: Boolean = false
) {
    val context = LocalContext.current
    var songInfo by remember { mutableStateOf<SongResponse?>(null) }
    var isLiked by remember { mutableStateOf(false) }
    var isShuffled by remember { mutableStateOf(false) }
    var isRepeating by remember { mutableStateOf(false) }
    var showCommentSheet by remember { mutableStateOf(false) }
    var showLyrics by remember { mutableStateOf(false) }
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var menuTab by remember { mutableStateOf(0) } // 0=곡정보, 1=앨범정보, 2=아티스트채널

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val songId = initialSongIndex
    val exoPlayer = remember { (context.applicationContext as App).exoPlayer }

    LaunchedEffect(Unit) {
        context.startService(Intent(context, PlaybackService::class.java))
    }

    LaunchedEffect(songId) {
        AppPlayer.currentSongId.intValue = songId
        try {
            val res = RetrofitClient.api.getSong(songId)
            if (res.isSuccessful) {
                songInfo = res.body()
                val info = res.body()
                val artworkUri = info?.cover_path?.let { Uri.parse(RetrofitClient.coverUrl(it)) }
                val metadata = MediaMetadata.Builder()
                    .setTitle(blindTitle(songId))
                    .setArtist(info?.uploader ?: "")
                    .setArtworkUri(artworkUri)
                    .build()
                val mediaItem = MediaItem.Builder()
                    .setUri(RetrofitClient.streamUrl(songId))
                    .setMediaMetadata(metadata)
                    .build()
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                if (autoPlay) { exoPlayer.play(); AppPlayer.isPlaying.value = true }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    val isPlaying = AppPlayer.isPlaying.value
    LaunchedEffect(isPlaying) {
        if (isPlaying) exoPlayer.play() else exoPlayer.pause()
    }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(500L)
            val dur = exoPlayer.duration.takeIf { it > 0 } ?: 0L
            val pos = exoPlayer.currentPosition
            AppPlayer.duration.longValue = dur
            if (dur > 0) AppPlayer.sliderPosition.floatValue = pos.toFloat() / dur.toFloat()
            if (exoPlayer.playbackState == androidx.media3.common.Player.STATE_ENDED) {
                AppPlayer.isPlaying.value = false
                AppPlayer.sliderPosition.floatValue = 0f
            }
        }
    }

    val sliderPosition = AppPlayer.sliderPosition.floatValue
    val duration = AppPlayer.duration.longValue
    val totalSeconds = (duration / 1000).toInt()
    val currentSeconds = (sliderPosition * totalSeconds).toInt()
    val currentTime = "%02d:%02d".format(currentSeconds / 60, currentSeconds % 60)
    val totalTime   = "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)

    var myRating by remember { mutableIntStateOf(0) }
    var avgRating by remember { mutableStateOf<String?>(null) }
    var pendingRating by remember { mutableIntStateOf(0) }
    var showRatingConfirm by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(songId) {
        try {
            val res = RetrofitClient.api.getRating(TokenManager.getBearerToken(), songId)
            if (res.isSuccessful) {
                myRating = res.body()?.my_score ?: 0
                avgRating = res.body()?.avg_score
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    LaunchedEffect(songId, songInfo) {
        if (songInfo == null) return@LaunchedEffect
        try {
            val res = RetrofitClient.api.getLikeStatus(TokenManager.getBearerToken(), songId)
            if (res.isSuccessful) {
                val liked = res.body()?.liked == true
                isLiked = liked
                val song = songInfo
                if (song != null) {
                    if (liked) LikedSongsManager.like(song) else LikedSongsManager.unlike(songId)
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    val revealed = myRating > 0
    val displayTitle = when {
        songInfo == null -> "로딩 중..."
        revealed -> songInfo!!.title ?: blindTitle(songId)
        else -> blindTitle(songId)
    }
    val displayArtist = when {
        songInfo == null -> ""
        revealed -> songInfo!!.uploader ?: blindArtist(songId)
        else -> blindArtist(songId)
    }

    LaunchedEffect(displayTitle, displayArtist) {
        AppPlayer.currentTitle.value = displayTitle
        AppPlayer.currentArtist.value = displayArtist
        // 락스크린 / 노티 메타데이터 동기화
        val current = exoPlayer.currentMediaItem ?: return@LaunchedEffect
        val artworkUri = songInfo?.cover_path?.let { Uri.parse(RetrofitClient.coverUrl(it)) }
        val updatedMetadata = MediaMetadata.Builder()
            .setTitle(displayTitle)
            .setArtist(displayArtist)
            .setArtworkUri(artworkUri)
            .build()
        val updatedItem = current.buildUpon().setMediaMetadata(updatedMetadata).build()
        exoPlayer.replaceMediaItem(0, updatedItem)
    }

    // ── 앨범 커버 애니메이션 (메뉴 열릴 때 위로 올라가며 축소) ─────
    val albumScale by animateFloatAsState(
        targetValue = if (showMenu) 0.55f else 1f,
        animationSpec = tween(380),
        label = "albumScale"
    )
    val albumTranslationY by animateFloatAsState(
        targetValue = if (showMenu) -80f else 0f,
        animationSpec = tween(380),
        label = "albumTransY"
    )

    // ── 가사 데이터 ──────────────────────────────────────────────────
    val nonEmptyLyrics = (songInfo?.lyrics?.split("\n") ?: emptyList()).filter { it.isNotBlank() }
    val lyricIdx = currentLyricIndex(currentSeconds, totalSeconds.coerceAtLeast(1), nonEmptyLyrics.size)
    val currentLyric = nonEmptyLyrics.getOrNull(lyricIdx) ?: ""
    val nextLyric    = nonEmptyLyrics.getOrNull(lyricIdx + 1) ?: ""

    Box(modifier = Modifier.fillMaxSize().background(NC_BG)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(52.dp))

            // ── 상단 바: 뒤로가기 | 제목 | 메뉴 ────────────────────
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "닫기",
                        modifier = Modifier.size(28.dp), tint = NC_TEXT)
                }
                Text(
                    text = displayTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    color = NC_TEXT,
                    modifier = Modifier.align(Alignment.Center).padding(horizontal = 56.dp)
                )
                IconButton(
                    onClick = { showMenu = !showMenu },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        if (showMenu) Icons.Default.Close else Icons.Default.MoreVert,
                        contentDescription = "메뉴",
                        modifier = Modifier.size(26.dp),
                        tint = if (showMenu) NC_PRIMARY else NC_TEXT
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── 앨범 표지 ────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .graphicsLayer {
                        scaleX = albumScale
                        scaleY = albumScale
                        translationY = albumTranslationY
                    }
                    .clip(RoundedCornerShape(0.dp))
            ) {
                val coverUrl = if (revealed) songInfo?.cover_path?.let { RetrofitClient.coverUrl(it) } else null
                if (coverUrl != null) {
                    AsyncImage(
                        model = coverUrl,
                        contentDescription = "앨범 표지",
                        modifier = Modifier.matchParentSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Brush.linearGradient(listOf(NC_PRIMARY, NC_ACCENT))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.MusicNote, contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = NC_BG.copy(alpha = if (revealed) 1f else 0.15f))
                    }
                }
                if (!revealed) {
                    Box(
                        modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.55f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔒", fontSize = 48.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("별점을 남기면 공개됩니다",
                                color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp,
                                fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // ── 메뉴 패널 (앨범 아래에서 펼쳐짐) ────────────────────
            AnimatedVisibility(
                visible = showMenu,
                enter = expandVertically(tween(340)) + fadeIn(tween(340)),
                exit  = shrinkVertically(tween(280)) + fadeOut(tween(280))
            ) {
                PlayerMenuPanel(
                    menuTab = menuTab,
                    onTabChange = { menuTab = it },
                    isLiked = isLiked,
                    onLikeClick = {
                        val newLiked = !isLiked
                        isLiked = newLiked
                        scope.launch {
                            try {
                                RetrofitClient.api.toggleLike(TokenManager.getBearerToken(), songId)
                                val song = songInfo
                                if (song != null) {
                                    if (newLiked) LikedSongsManager.like(song)
                                    else LikedSongsManager.unlike(songId)
                                }
                            } catch (e: Exception) { isLiked = !newLiked }
                        }
                    },
                    onAddToPlaylist = { showAddToPlaylistDialog = true },
                    songInfo = songInfo,
                    songId = songId,
                    revealed = revealed,
                    displayTitle = displayTitle,
                    displayArtist = displayArtist
                )
            }

            // ── 가사 2줄 미리보기 (메뉴 닫혔을 때만 표시) ───────────
            AnimatedVisibility(
                visible = !showMenu,
                enter = fadeIn(tween(300)),
                exit  = fadeOut(tween(200))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .clickable { showLyrics = true }
                ) {
                    if (currentLyric.isNotBlank()) {
                        Text(
                            text = currentLyric,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontFamily = InterFontFamily
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = nextLyric.ifBlank { "· · ·" },
                            color = Color(0xFFA1A1A1),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontFamily = InterFontFamily
                        )
                    } else {
                        Spacer(Modifier.height(38.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            TextButton(
                onClick = { showLyrics = !showLyrics },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = songInfo?.genre ?: "",
                    fontSize = 15.sp, fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center, maxLines = 1,
                    overflow = TextOverflow.Ellipsis, color = NC_SUBTEXT
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ── 댓글 / 좋아요 / 플리담기 ─────────────────────────────
            Row(modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(
                    onClick = { if (revealed) showCommentSheet = true },
                    enabled = revealed
                ) {
                    Icon(Icons.Rounded.ChatBubbleOutline, contentDescription = "댓글",
                        tint = if (revealed) NC_TEXT else NC_SUBTEXT.copy(alpha = 0.3f))
                }
                Row {
                    IconButton(onClick = {
                        val newLiked = !isLiked
                        isLiked = newLiked
                        scope.launch {
                            try {
                                RetrofitClient.api.toggleLike(TokenManager.getBearerToken(), songId)
                                val song = songInfo
                                if (song != null) {
                                    if (newLiked) LikedSongsManager.like(song)
                                    else LikedSongsManager.unlike(songId)
                                }
                            } catch (e: Exception) { isLiked = !newLiked }
                        }
                    }) {
                        Icon(
                            if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "좋아요",
                            tint = if (isLiked) Color(0xFFFF0000) else NC_SUBTEXT
                        )
                    }
                    IconButton(onClick = { showAddToPlaylistDialog = true }) {
                        Icon(Icons.Rounded.AddCircleOutline, contentDescription = "플리 담기", tint = NC_SUBTEXT)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── 별점 ─────────────────────────────────────────────────
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = when {
                        !revealed -> "★ 별점을 누르면 곡 정보가 공개됩니다"
                        avgRating != null -> "공개됨 · 평균 ★ $avgRating"
                        else -> "공개됨"
                    },
                    fontSize = 12.sp,
                    color = if (revealed) NC_PRIMARY else NC_SUBTEXT
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.Center) {
                    (1..5).forEach { star ->
                        IconButton(
                            onClick = {
                                if (!revealed) { pendingRating = star; showRatingConfirm = true }
                            },
                            enabled = !revealed,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (star <= (if (showRatingConfirm) pendingRating else myRating))
                                    Icons.Filled.Star else Icons.Filled.StarBorder,
                                contentDescription = "$star 점",
                                tint = if (star <= (if (showRatingConfirm) pendingRating else myRating))
                                    NC_PRIMARY else NC_SUBTEXT,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ── 슬라이더 ─────────────────────────────────────────────
            Slider(
                value = sliderPosition,
                onValueChange = { v ->
                    AppPlayer.sliderPosition.floatValue = v
                    exoPlayer.seekTo((v * duration).toLong())
                },
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = NC_PRIMARY,
                    activeTrackColor = NC_PRIMARY,
                    inactiveTrackColor = NC_SUBTEXT.copy(alpha = 0.3f)
                )
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(currentTime, fontSize = 12.sp, color = NC_SUBTEXT)
                Text(totalTime,   fontSize = 12.sp, color = NC_SUBTEXT)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── 재생 컨트롤 ──────────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { isShuffled = !isShuffled }) {
                    Icon(Icons.Rounded.Shuffle, contentDescription = "셔플",
                        tint = if (isShuffled) NC_PRIMARY else NC_SUBTEXT,
                        modifier = Modifier.size(24.dp))
                }
                IconButton(onClick = {
                    if (onPrevTrack != null) onPrevTrack(songId - 1)
                    else exoPlayer.seekBack()
                }) {
                    Icon(Icons.Rounded.SkipPrevious, contentDescription = "이전 곡",
                        modifier = Modifier.size(36.dp), tint = NC_TEXT)
                }
                Box(
                    modifier = Modifier.size(64.dp).clip(CircleShape).background(NC_PRIMARY)
                        .clickable { AppPlayer.isPlaying.value = !AppPlayer.isPlaying.value },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = null, tint = NC_BG, modifier = Modifier.size(36.dp)
                    )
                }
                IconButton(onClick = {
                    if (onNextTrack != null) onNextTrack(songId + 1)
                    else exoPlayer.seekForward()
                }) {
                    Icon(Icons.Rounded.SkipNext, contentDescription = "다음 곡",
                        modifier = Modifier.size(36.dp), tint = NC_TEXT)
                }
                IconButton(onClick = { isRepeating = !isRepeating }) {
                    Icon(Icons.Rounded.Repeat, contentDescription = "반복",
                        tint = if (isRepeating) NC_PRIMARY else NC_SUBTEXT,
                        modifier = Modifier.size(24.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // ── 평점 확인 모달 ─────────────────────────────────────────
        if (showRatingConfirm) {
            AlertDialog(
                onDismissRequest = { showRatingConfirm = false; pendingRating = 0 },
                containerColor = NC_SURFACE,
                title = { Text("평점 제출 확인", color = NC_TEXT, fontWeight = FontWeight.Bold) },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("평점은 한 번만 남길 수 있습니다.\n이대로 제출하시겠습니까?",
                            color = NC_SUBTEXT, fontSize = 14.sp, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.Center) {
                            repeat(5) { i ->
                                Icon(
                                    if (i < pendingRating) Icons.Filled.Star else Icons.Filled.StarBorder,
                                    contentDescription = null, tint = NC_PRIMARY,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Text("$pendingRating / 5점", color = NC_PRIMARY, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        showRatingConfirm = false
                        myRating = pendingRating
                        val star = pendingRating
                        scope.launch {
                            try {
                                val res = RetrofitClient.api.rateSong(
                                    TokenManager.getBearerToken(), songId,
                                    com.example.noisecanseling.network.RatingRequest(star)
                                )
                                if (res.isSuccessful) avgRating = res.body()?.avg_score
                            } catch (e: Exception) { e.printStackTrace() }
                        }
                    }) { Text("제출", color = NC_PRIMARY, fontWeight = FontWeight.Bold) }
                },
                dismissButton = {
                    TextButton(onClick = { showRatingConfirm = false; pendingRating = 0 }) {
                        Text("취소", color = NC_SUBTEXT)
                    }
                }
            )
        }

        if (showAddToPlaylistDialog) {
            AddToPlaylistDialog(songTitle = displayTitle, onDismiss = { showAddToPlaylistDialog = false })
        }

        // ── 전체 가사 오버레이 ──────────────────────────────────────
        if (showLyrics) {
            LyricsOverlay(
                displayTitle = displayTitle,
                displayArtist = displayArtist,
                lyrics = nonEmptyLyrics,
                currentIndex = lyricIdx,
                isPlaying = isPlaying,
                onClose = { showLyrics = false },
                onPrev = { if (onPrevTrack != null) onPrevTrack(songId - 1) },
                onNext = { if (onNextTrack != null) onNextTrack(songId + 1) }
            )
        }

        // ── 댓글 바텀시트 ───────────────────────────────────────────
        if (showCommentSheet) {
            CommentBottomSheet(sheetState = sheetState, songId = songId,
                onDismiss = { showCommentSheet = false })
        }
    }
}

// ── 메뉴 패널 ─────────────────────────────────────────────────────────
@Composable
private fun PlayerMenuPanel(
    menuTab: Int,
    onTabChange: (Int) -> Unit,
    isLiked: Boolean,
    onLikeClick: () -> Unit,
    onAddToPlaylist: () -> Unit,
    songInfo: SongResponse?,
    songId: Int,
    revealed: Boolean,
    displayTitle: String,
    displayArtist: String
) {
    val tabs = listOf("곡 정보", "앨범 정보", "아티스트 채널")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(0.dp))
            .background(NC_SURFACE)
            .padding(16.dp)
    ) {
        // 액션 버튼 2개 (플레이리스트 담기 + 좋아요)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onAddToPlaylist,
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NC_PRIMARY)
            ) {
                Icon(Icons.Rounded.AddCircleOutline, contentDescription = null,
                    tint = NC_BG, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("플레이리스트 담기", color = NC_BG, fontSize = 13.sp,
                    fontWeight = FontWeight.Bold, maxLines = 1)
            }
            Button(
                onClick = onLikeClick,
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLiked) Color(0xFFFF0000) else NC_SURFACE
                ),
                border = if (!isLiked) androidx.compose.foundation.BorderStroke(
                    1.dp, NC_SUBTEXT.copy(alpha = 0.4f)
                ) else null
            ) {
                Icon(
                    if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = null,
                    tint = if (isLiked) Color.White else NC_SUBTEXT,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    if (isLiked) "좋아요 취소" else "좋아요",
                    color = if (isLiked) Color.White else NC_SUBTEXT,
                    fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // 탭 선택
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(0.dp))
                .background(NC_BG)
        ) {
            tabs.forEachIndexed { i, label ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTabChange(i) }
                        .background(if (menuTab == i) NC_PRIMARY.copy(alpha = 0.15f) else Color.Transparent)
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        color = if (menuTab == i) NC_PRIMARY else NC_SUBTEXT,
                        fontSize = 12.sp,
                        fontWeight = if (menuTab == i) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // 탭 하단 인디케이터
        Row(modifier = Modifier.fillMaxWidth()) {
            tabs.forEachIndexed { i, _ ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(if (menuTab == i) NC_PRIMARY else Color.Transparent)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // 탭 내용
        when (menuTab) {
            0 -> MenuTabSongInfo(songInfo, songId, revealed, displayTitle, displayArtist)
            1 -> MenuTabAlbumInfo(songInfo, displayTitle)
            2 -> MenuTabArtistChannel(songInfo, revealed)
        }
    }
}

@Composable
private fun MenuTabSongInfo(
    songInfo: SongResponse?,
    songId: Int,
    revealed: Boolean,
    displayTitle: String,
    displayArtist: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MenuInfoRow("제목", if (revealed) displayTitle else "별점 공개 후 확인 가능")
        MenuInfoRow("아티스트", if (revealed) displayArtist else "비공개")
        MenuInfoRow("장르", songInfo?.genre ?: "-")
        MenuInfoRow("재생수", "%,d".format(songInfo?.play_count ?: 0))
        MenuInfoRow("좋아요", "%,d".format(songInfo?.like_count ?: 0))
        MenuInfoRow("댓글", "%,d".format(songInfo?.comment_count ?: 0))
    }
}

@Composable
private fun MenuTabAlbumInfo(songInfo: SongResponse?, displayTitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MenuInfoRow("앨범명", displayTitle)
        MenuInfoRow("발매일", songInfo?.created_at?.take(10) ?: "-")
        MenuInfoRow("트랙", "01")
    }
}

@Composable
private fun MenuTabArtistChannel(songInfo: SongResponse?, revealed: Boolean) {
    val artistName = songInfo?.uploader
    val isFollowing = if (artistName != null) FollowManager.isFollowing(artistName) else false
    val scope = rememberCoroutineScope()

    LaunchedEffect(artistName, revealed) {
        if (revealed && artistName != null && isLoggedIn) {
            FollowManager.loadFollowStatus(artistName)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (!revealed || artistName == null) {
            Text(
                "아티스트 채널은 별점 공개 후 방문할 수 있습니다.",
                color = NC_SUBTEXT, fontSize = 13.sp, fontFamily = InterFontFamily
            )
        } else {
            MenuInfoRow("아티스트", artistName)
            if (songInfo.genre != null) MenuInfoRow("장르", songInfo.genre)

            Spacer(Modifier.height(4.dp))

            Button(
                onClick = {
                    if (isLoggedIn) scope.launch { FollowManager.toggle(artistName) }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFollowing) NC_SURFACE else NC_PRIMARY
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    if (isFollowing) Icons.Default.Check else Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = if (isFollowing) NC_SUBTEXT else NC_BG,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    if (isFollowing) "팔로잉" else "팔로우",
                    color = if (isFollowing) NC_SUBTEXT else NC_BG,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun MenuInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(label, color = NC_SUBTEXT, fontSize = 13.sp,
            modifier = Modifier.width(72.dp), fontFamily = InterFontFamily)
        Text(value, color = NC_TEXT, fontSize = 13.sp,
            fontFamily = InterFontFamily, modifier = Modifier.weight(1f))
    }
}

// ── 전체 가사 오버레이 ────────────────────────────────────────────────
@Composable
private fun LyricsOverlay(
    displayTitle: String,
    displayArtist: String,
    lyrics: List<String>,
    currentIndex: Int,
    isPlaying: Boolean,
    onClose: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().background(NC_BG).padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(52.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onClose, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "닫기",
                    modifier = Modifier.size(28.dp), tint = NC_TEXT)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.Center)) {
                Text(displayTitle, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = NC_TEXT)
                Text(displayArtist, fontSize = 12.sp, color = NC_SUBTEXT)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        if (lyrics.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("가사 정보 없음", fontSize = 16.sp, color = NC_SUBTEXT)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(lyrics.indices.toList()) { i ->
                    val line = lyrics[i]
                    Text(
                        text = line.ifBlank { " " },
                        color = when {
                            i == currentIndex -> Color.White
                            i == currentIndex + 1 -> Color(0xFFA1A1A1)
                            else -> NC_SUBTEXT.copy(alpha = 0.45f)
                        },
                        fontSize = when {
                            i == currentIndex -> 17.sp
                            i == currentIndex + 1 -> 14.sp
                            else -> 13.sp
                        },
                        fontWeight = if (i == currentIndex) FontWeight.SemiBold else FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        fontFamily = InterFontFamily
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPrev) {
                Icon(Icons.Rounded.SkipPrevious, contentDescription = "이전",
                    modifier = Modifier.size(36.dp), tint = NC_TEXT)
            }
            Box(
                modifier = Modifier.size(56.dp).clip(CircleShape).background(NC_PRIMARY)
                    .clickable { AppPlayer.isPlaying.value = !AppPlayer.isPlaying.value },
                contentAlignment = Alignment.Center
            ) {
                Icon(if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = null, tint = NC_BG, modifier = Modifier.size(32.dp))
            }
            IconButton(onClick = onNext) {
                Icon(Icons.Rounded.SkipNext, contentDescription = "다음",
                    modifier = Modifier.size(36.dp), tint = NC_TEXT)
            }
        }
    }
}

// ── 댓글 바텀시트 ─────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentBottomSheet(sheetState: SheetState, songId: Int, onDismiss: () -> Unit) {
    var commentText by remember { mutableStateOf("") }
    val comments = remember { mutableStateListOf<com.example.noisecanseling.network.CommentResponse>() }
    val scope = rememberCoroutineScope()
    val myNickname = TokenManager.getNickname()
    val commentLikes = remember { mutableStateMapOf<Int, Int>() }
    val commentLiked = remember { mutableStateSetOf<Int>() }
    var sortMode by remember { mutableStateOf("최신순") }

    val displayedComments = when (sortMode) {
        "좋아요순" -> comments.sortedByDescending { commentLikes[it.id] ?: 0 }
        else -> comments.toList()
    }

    LaunchedEffect(songId) {
        try {
            val res = RetrofitClient.api.getComments(songId)
            if (res.isSuccessful) { comments.clear(); comments.addAll(res.body() ?: emptyList()) }
        } catch (e: Exception) { e.printStackTrace() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = NC_SURFACE,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(modifier = Modifier.fillMaxHeight(0.85f)) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Text("댓글 ${comments.size}개", fontWeight = FontWeight.Bold, fontSize = 16.sp,
                    color = NC_TEXT, modifier = Modifier.weight(1f))
                TextButton(onClick = { sortMode = if (sortMode == "최신순") "좋아요순" else "최신순" }) {
                    Text(sortMode, color = NC_PRIMARY, fontSize = 12.sp)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "닫기", tint = NC_SUBTEXT)
                }
            }
            HorizontalDivider(color = NC_SUBTEXT.copy(alpha = 0.2f))
            LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)) {
                items(displayedComments, key = { it.id }) { comment ->
                    ApiCommentItem(
                        comment = comment,
                        isMine = comment.nickname == myNickname,
                        likeCount = commentLikes[comment.id] ?: 0,
                        isLiked = comment.id in commentLiked,
                        onLike = {
                            if (comment.id in commentLiked) {
                                commentLiked.remove(comment.id)
                                commentLikes[comment.id] = maxOf(0, (commentLikes[comment.id] ?: 1) - 1)
                            } else {
                                commentLiked.add(comment.id)
                                commentLikes[comment.id] = (commentLikes[comment.id] ?: 0) + 1
                            }
                        },
                        onDelete = {
                            scope.launch {
                                try {
                                    val res = RetrofitClient.api.deleteComment(
                                        TokenManager.getBearerToken(), songId, comment.id)
                                    if (res.isSuccessful) comments.remove(comment)
                                } catch (e: Exception) { e.printStackTrace() }
                            }
                        }
                    )
                }
            }
            HorizontalDivider(color = NC_SUBTEXT.copy(alpha = 0.2f))
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = commentText, onValueChange = { commentText = it },
                    placeholder = { Text("댓글을 입력하세요", fontSize = 14.sp, color = NC_SUBTEXT) },
                    singleLine = true, shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.weight(1f),
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = NC_TEXT),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NC_PRIMARY, unfocusedBorderColor = NC_SUBTEXT, cursorColor = NC_PRIMARY)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        val text = commentText.trim()
                        if (text.isNotBlank()) {
                            commentText = ""
                            scope.launch {
                                try {
                                    val res = RetrofitClient.api.addComment(
                                        TokenManager.getBearerToken(), songId,
                                        com.example.noisecanseling.network.AddCommentRequest(text))
                                    if (res.isSuccessful) {
                                        myLocalComments.add(0, LocalComment(songId, blindTitle(songId), text))
                                        val reload = RetrofitClient.api.getComments(songId)
                                        if (reload.isSuccessful) {
                                            comments.clear(); comments.addAll(reload.body() ?: emptyList())
                                        }
                                    }
                                } catch (e: Exception) { e.printStackTrace() }
                            }
                        }
                    },
                    enabled = commentText.isNotBlank()
                ) {
                    Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "전송",
                        tint = if (commentText.isNotBlank()) NC_PRIMARY else NC_SUBTEXT.copy(alpha = 0.4f))
                }
            }
            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.ime))
        }
    }
}

@Composable
fun ApiCommentItem(
    comment: com.example.noisecanseling.network.CommentResponse,
    isMine: Boolean,
    likeCount: Int = 0,
    isLiked: Boolean = false,
    onLike: () -> Unit = {},
    onDelete: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(NC_SURFACE),
            contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Person, contentDescription = null,
                modifier = Modifier.size(20.dp), tint = NC_SUBTEXT)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(comment.nickname, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                color = if (isMine) NC_PRIMARY else NC_SUBTEXT)
            Text(comment.content, fontSize = 14.sp, color = NC_TEXT)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(onClick = onLike, modifier = Modifier.size(32.dp)) {
                Icon(
                    if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "댓글 좋아요",
                    tint = if (isLiked) Color(0xFFFF0000) else NC_SUBTEXT,
                    modifier = Modifier.size(16.dp)
                )
            }
            if (likeCount > 0) Text("$likeCount", fontSize = 10.sp, color = NC_SUBTEXT)
        }
        if (isMine) {
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "삭제",
                    modifier = Modifier.size(18.dp), tint = Color(0xFFFF6B6B))
            }
        }
    }
}

@Composable
fun AddToPlaylistDialog(songTitle: String, onDismiss: () -> Unit) {
    var showNewPlaylistField by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var addedTo by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("재생목록에 추가", fontWeight = FontWeight.SemiBold) },
        text = {
            Column {
                Text("\"$songTitle\"을(를) 추가할 목록을 선택하세요",
                    fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                PlaylistManager.playlists.forEach { playlist ->
                    val isAdded = addedTo == playlist.name
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(0.dp))
                            .clickable { if (!isAdded) addedTo = playlist.name }
                            .background(
                                if (isAdded) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (isAdded) Icons.Default.CheckCircle else Icons.Rounded.QueueMusic,
                            contentDescription = null, modifier = Modifier.size(20.dp),
                            tint = if (isAdded) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(playlist.name, fontSize = 14.sp,
                            fontWeight = if (isAdded) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isAdded) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f))
                        Text("${playlist.songs.size}곡", fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
                Spacer(modifier = Modifier.height(4.dp))
                if (showNewPlaylistField) {
                    OutlinedTextField(
                        value = newPlaylistName, onValueChange = { newPlaylistName = it },
                        label = { Text("새 재생목록 이름") }, singleLine = true,
                        shape = RoundedCornerShape(0.dp), modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = {
                                if (newPlaylistName.isNotBlank()) {
                                    PlaylistManager.addPlaylist(newPlaylistName.trim())
                                    addedTo = newPlaylistName.trim()
                                    newPlaylistName = ""; showNewPlaylistField = false
                                }
                            }) { Icon(Icons.Default.Check, contentDescription = "확인",
                                tint = MaterialTheme.colorScheme.primary) }
                        }
                    )
                } else {
                    TextButton(onClick = { showNewPlaylistField = true }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("새 재생목록 만들기")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(if (addedTo != null) "완료" else "닫기") }
        }
    )
}

@UnstableApi
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "플레이어 화면")
@Composable
fun PlayerScreenPreview() {
    MaterialTheme { PlayerScreen() }
}
