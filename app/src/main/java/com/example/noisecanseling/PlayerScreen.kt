package com.example.noisecanseling

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.noisecanseling.network.RetrofitClient
import com.example.noisecanseling.network.SongResponse
import com.example.noisecanseling.network.TokenManager
import kotlinx.coroutines.launch

private val NC_BG = Color(0xFF0E1117)
private val NC_SURFACE = Color(0xFF171B24)
private val NC_PRIMARY = Color(0xFFFFC857)
private val NC_ACCENT = Color(0xFF58C4DD)
private val NC_TEXT = Color.White
private val NC_SUBTEXT = Color(0xFF9BA3B4)

// 곡 데이터
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

// 샘플 댓글 데이터
data class Comment(val nickname: String, val content: String, var likeCount: Int = 0)

val sampleComments = listOf(
    Comment("익명1", "진짜 명곡이다 ㅠㅠ"),
    Comment("익명2", "이 노래 들으면 항상 눈물남"),
    Comment("익명3", "멜로디가 너무 좋아요"),
    Comment("익명4", "반복재생 100번째"),
    Comment("익명5", "가사가 진짜 위로가 됨"),
    Comment("익명6", "이 앨범 전체 다 좋음"),
    Comment("익명7", "처음 들었는데 바로 최애곡됨"),
    Comment("익명8", "작업할 때 항상 틀어놓는 노래"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    onBackClick: () -> Unit = {},
    onPlaylistClick: () -> Unit = {},
    initialSongIndex: Int = 0,
    autoPlay: Boolean = false
) {
    val context = LocalContext.current
    var songInfo by remember { mutableStateOf<SongResponse?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableFloatStateOf(0f) }
    var isLiked by remember { mutableStateOf(false) }
    var isShuffled by remember { mutableStateOf(false) }
    var isRepeating by remember { mutableStateOf(false) }
    var showCommentSheet by remember { mutableStateOf(false) }
    var showLyrics by remember { mutableStateOf(false) }
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var duration by remember { mutableLongStateOf(0L) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val songId = initialSongIndex

    // ExoPlayer 설정
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }
    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    // 곡 정보 로드 & 스트리밍 시작
    LaunchedEffect(songId) {
        try {
            val res = RetrofitClient.api.getSong(songId)
            if (res.isSuccessful) {
                songInfo = res.body()
                val streamUrl = RetrofitClient.streamUrl(songId)
                val mediaItem = MediaItem.fromUri(streamUrl)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                if (autoPlay) exoPlayer.play()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 재생 상태 동기화
    LaunchedEffect(isPlaying) {
        if (isPlaying) exoPlayer.play() else exoPlayer.pause()
    }

    // 슬라이더 업데이트 (버퍼링 중에는 isPlaying 건드리지 않음)
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(500L)
            val dur = exoPlayer.duration.takeIf { it > 0 } ?: 0L
            val pos = exoPlayer.currentPosition
            duration = dur
            if (dur > 0) sliderPosition = pos.toFloat() / dur.toFloat()
            // 재생이 끝났을 때만 isPlaying = false
            if (exoPlayer.playbackState == androidx.media3.common.Player.STATE_ENDED) {
                isPlaying = false
                sliderPosition = 0f
            }
        }
    }

    val totalSeconds = (duration / 1000).toInt()
    val currentSeconds = (sliderPosition * totalSeconds).toInt()
    val currentTime = "%02d:%02d".format(currentSeconds / 60, currentSeconds % 60)
    val totalTime = "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)

    val currentSong = songInfo

    // 별점 상태
    var myRating by remember { mutableIntStateOf(0) }
    var avgRating by remember { mutableStateOf<String?>(null) }
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

    // 별점 누르기 전: 블라인드, 누른 후: 실제 정보 공개
    val revealed = myRating > 0
    val displayTitle = when {
        currentSong == null -> "로딩 중..."
        revealed -> currentSong.title
        else -> blindTitle(songId)
    }
    val displayArtist = when {
        currentSong == null -> ""
        revealed -> currentSong.uploader
        else -> blindArtist(songId)
    }

    // 앨범 표지 크기 애니메이션
    val albumSize by animateDpAsState(
        targetValue = if (showLyrics) 80.dp else 260.dp,
        animationSpec = tween(400),
        label = "albumSize"
    )

    Box(modifier = Modifier.fillMaxSize().background(NC_BG)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(52.dp))

            // 상단 뒤로가기 + 제목 + 재생목록 버튼
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onBackClick, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "닫기", modifier = Modifier.size(28.dp), tint = NC_TEXT)
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
                IconButton(onClick = onPlaylistClick, modifier = Modifier.align(Alignment.CenterEnd)) {
                    Icon(Icons.Rounded.QueueMusic, contentDescription = "재생목록", modifier = Modifier.size(26.dp), tint = NC_TEXT)
                }
            }

            // 앨범 표지를 화면 중앙으로 밀어내는 여백
            Spacer(modifier = Modifier.weight(1f))

            // 앨범 표지
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(NC_PRIMARY, NC_ACCENT))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.MusicNote, contentDescription = null,
                    modifier = Modifier.size(80.dp), tint = NC_BG)
            }

            Spacer(modifier = Modifier.weight(1f))

            TextButton(onClick = { showLyrics = !showLyrics }, modifier = Modifier.fillMaxWidth()) {
                Text(text = currentSong?.genre ?: "", fontSize = 15.sp, fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis, color = NC_SUBTEXT)
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 댓글 / 좋아요 / 플리담기
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(
                    onClick = { if (revealed) showCommentSheet = true },
                    enabled = revealed
                ) {
                    Icon(
                        Icons.Rounded.ChatBubbleOutline,
                        contentDescription = "댓글",
                        tint = if (revealed) NC_TEXT else NC_SUBTEXT.copy(alpha = 0.3f)
                    )
                }
                Row {
                    IconButton(onClick = { isLiked = !isLiked }) {
                        Icon(
                            if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "좋아요",
                            tint = if (isLiked) Color(0xFFE91E63) else NC_SUBTEXT
                        )
                    }
                    IconButton(onClick = { showAddToPlaylistDialog = true }) {
                        Icon(Icons.Rounded.AddCircleOutline, contentDescription = "플리 담기", tint = NC_SUBTEXT)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 별점
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
                                if (!revealed) {
                                    myRating = star
                                    scope.launch {
                                        try {
                                            val res = RetrofitClient.api.rateSong(
                                                TokenManager.getBearerToken(), songId,
                                                com.example.noisecanseling.network.RatingRequest(star)
                                            )
                                            if (res.isSuccessful) avgRating = res.body()?.avg_score
                                        } catch (e: Exception) { e.printStackTrace() }
                                    }
                                }
                            },
                            enabled = !revealed,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (star <= myRating) Icons.Filled.Star else Icons.Filled.StarBorder,
                                contentDescription = "$star 점",
                                tint = if (star <= myRating) NC_PRIMARY else NC_SUBTEXT,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 슬라이더
            Slider(value = sliderPosition, onValueChange = { sliderPosition = it
                exoPlayer.seekTo((it * duration).toLong()) },
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(thumbColor = NC_PRIMARY, activeTrackColor = NC_PRIMARY, inactiveTrackColor = NC_SUBTEXT.copy(alpha = 0.3f)))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(currentTime, fontSize = 12.sp, color = NC_SUBTEXT)
                Text(totalTime, fontSize = 12.sp, color = NC_SUBTEXT)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 재생 컨트롤러
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { isShuffled = !isShuffled }) {
                    Icon(Icons.Rounded.Shuffle, contentDescription = "셔플",
                        tint = if (isShuffled) NC_PRIMARY else NC_SUBTEXT, modifier = Modifier.size(24.dp))
                }
                IconButton(onClick = { exoPlayer.seekBack() }) {
                    Icon(Icons.Rounded.SkipPrevious, contentDescription = "이전 곡", modifier = Modifier.size(36.dp), tint = NC_TEXT)
                }
                Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(NC_PRIMARY).clickable { isPlaying = !isPlaying },
                    contentAlignment = Alignment.Center) {
                    Icon(imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = null, tint = NC_BG, modifier = Modifier.size(36.dp))
                }
                IconButton(onClick = { exoPlayer.seekForward() }) {
                    Icon(Icons.Rounded.SkipNext, contentDescription = "다음 곡", modifier = Modifier.size(36.dp), tint = NC_TEXT)
                }
                IconButton(onClick = { isRepeating = !isRepeating }) {
                    Icon(Icons.Rounded.Repeat, contentDescription = "반복",
                        tint = if (isRepeating) NC_PRIMARY else NC_SUBTEXT, modifier = Modifier.size(24.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // 플레이리스트 추가 다이얼로그
        if (showAddToPlaylistDialog) {
            AddToPlaylistDialog(
                songTitle = displayTitle,
                onDismiss = { showAddToPlaylistDialog = false }
            )
        }

        // 가사 전체화면 오버레이
        if (showLyrics) {
            Column(
                modifier = Modifier.fillMaxSize().background(NC_BG).padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(52.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    IconButton(onClick = { showLyrics = false }, modifier = Modifier.align(Alignment.CenterStart)) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "닫기", modifier = Modifier.size(28.dp), tint = NC_TEXT)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.align(Alignment.Center)) {
                        Text(displayTitle, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = NC_TEXT)
                        Text(displayArtist, fontSize = 12.sp, color = NC_SUBTEXT)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("가사 정보 없음", fontSize = 16.sp, color = NC_SUBTEXT)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {}) {
                        Icon(Icons.Rounded.SkipPrevious, contentDescription = "이전 곡", modifier = Modifier.size(36.dp), tint = NC_TEXT)
                    }
                    Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(NC_PRIMARY).clickable { isPlaying = !isPlaying },
                        contentAlignment = Alignment.Center) {
                        Icon(imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = null, tint = NC_BG, modifier = Modifier.size(32.dp))
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Rounded.SkipNext, contentDescription = "다음 곡", modifier = Modifier.size(36.dp), tint = NC_TEXT)
                    }
                }
            }
        }

        // 댓글 바텀 시트
        if (showCommentSheet) {
            CommentBottomSheet(
                sheetState = sheetState,
                songId = songId,
                onDismiss = { showCommentSheet = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentBottomSheet(
    sheetState: SheetState,
    songId: Int,
    onDismiss: () -> Unit
) {
    var commentText by remember { mutableStateOf("") }
    val comments = remember { mutableStateListOf<com.example.noisecanseling.network.CommentResponse>() }
    val scope = rememberCoroutineScope()
    val myNickname = TokenManager.getNickname()

    // #9: 댓글 좋아요 로컬 상태 (백엔드 미지원 → 클라이언트 관리)
    val commentLikes = remember { mutableStateMapOf<Int, Int>() }
    val commentLiked = remember { mutableStateSetOf<Int>() }
    // #9: 정렬 모드 (최신순 / 좋아요순)
    var sortMode by remember { mutableStateOf("최신순") }

    val displayedComments = when (sortMode) {
        "좋아요순" -> comments.sortedByDescending { commentLikes[it.id] ?: 0 }
        else -> comments.toList()
    }

    // 댓글 로드
    LaunchedEffect(songId) {
        try {
            val res = RetrofitClient.api.getComments(songId)
            if (res.isSuccessful) {
                comments.clear()
                comments.addAll(res.body() ?: emptyList())
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = NC_SURFACE,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(modifier = Modifier.fillMaxHeight(0.85f)) {
            // #9: 헤더에 정렬 버튼 추가
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("댓글 ${comments.size}개", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NC_TEXT, modifier = Modifier.weight(1f))
                TextButton(onClick = { sortMode = if (sortMode == "최신순") "좋아요순" else "최신순" }) {
                    Text(sortMode, color = NC_PRIMARY, fontSize = 12.sp)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "닫기", tint = NC_SUBTEXT)
                }
            }

            HorizontalDivider(color = NC_SUBTEXT.copy(alpha = 0.2f))

            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(displayedComments, key = { it.id }) { comment ->
                    ApiCommentItem(
                        comment = comment,
                        isMine = comment.nickname == myNickname,
                        likeCount = commentLikes[comment.id] ?: 0,
                        isLiked = comment.id in commentLiked,
                        onLike = {
                            // #9: 좋아요 토글
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
                                        TokenManager.getBearerToken(), songId, comment.id
                                    )
                                    if (res.isSuccessful) comments.remove(comment)
                                } catch (e: Exception) { e.printStackTrace() }
                            }
                        }
                    )
                }
            }

            HorizontalDivider(color = NC_SUBTEXT.copy(alpha = 0.2f))

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = commentText, onValueChange = { commentText = it },
                    placeholder = { Text("댓글을 입력하세요", fontSize = 14.sp, color = NC_SUBTEXT) },
                    singleLine = true, shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.weight(1f),
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = NC_TEXT),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NC_PRIMARY, unfocusedBorderColor = NC_SUBTEXT, cursorColor = NC_PRIMARY)
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
                                        com.example.noisecanseling.network.AddCommentRequest(text)
                                    )
                                    if (res.isSuccessful) {
                                        val reload = RetrofitClient.api.getComments(songId)
                                        if (reload.isSuccessful) {
                                            comments.clear()
                                            comments.addAll(reload.body() ?: emptyList())
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

// #9: 댓글 아이템 – 좋아요 버튼 + 좋아요 수 표시
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
        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(NC_SURFACE), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(20.dp), tint = NC_SUBTEXT)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(comment.nickname, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (isMine) NC_PRIMARY else NC_SUBTEXT)
            Text(comment.content, fontSize = 14.sp, color = NC_TEXT)
        }
        // 좋아요 버튼 + 수
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(onClick = onLike, modifier = Modifier.size(32.dp)) {
                Icon(
                    if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "댓글 좋아요",
                    tint = if (isLiked) Color(0xFFE91E63) else NC_SUBTEXT,
                    modifier = Modifier.size(16.dp)
                )
            }
            if (likeCount > 0) {
                Text("$likeCount", fontSize = 10.sp, color = NC_SUBTEXT)
            }
        }
        if (isMine) {
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "삭제", modifier = Modifier.size(18.dp), tint = Color(0xFFFF6B6B))
            }
        }
    }
}

@Composable
fun AddToPlaylistDialog(
    songTitle: String,
    onDismiss: () -> Unit
) {
    var showNewPlaylistField by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var addedTo by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("재생목록에 추가", fontWeight = FontWeight.SemiBold) },
        text = {
            Column {
                Text(
                    text = "\"$songTitle\"을(를) 추가할 목록을 선택하세요",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                // 기존 플레이리스트 목록
                PlaylistManager.playlists.forEach { playlist ->
                    val isAdded = addedTo == playlist.name
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                if (!isAdded) {
                                    addedTo = playlist.name
                                }
                            }
                            .background(
                                if (isAdded) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (isAdded) Icons.Default.CheckCircle else Icons.Rounded.QueueMusic,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = if (isAdded) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = playlist.name,
                            fontSize = 14.sp,
                            fontWeight = if (isAdded) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isAdded) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${playlist.songs.size}곡",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 새 재생목록 만들기
                if (showNewPlaylistField) {
                    OutlinedTextField(
                        value = newPlaylistName,
                        onValueChange = { newPlaylistName = it },
                        label = { Text("새 재생목록 이름") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = {
                                if (newPlaylistName.isNotBlank()) {
                                    PlaylistManager.addPlaylist(newPlaylistName.trim())
                                    addedTo = newPlaylistName.trim()
                                    newPlaylistName = ""
                                    showNewPlaylistField = false
                                }
                            }) {
                                Icon(Icons.Default.Check, contentDescription = "확인",
                                    tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    )
                } else {
                    TextButton(
                        onClick = { showNewPlaylistField = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("새 재생목록 만들기")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (addedTo != null) "완료" else "닫기")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "플레이어 화면")
@Composable
fun PlayerScreenPreview() {
    MaterialTheme { PlayerScreen() }
}
