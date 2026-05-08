package com.example.noisecanseling

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.ui.platform.LocalConfiguration
import coil.compose.AsyncImage
import com.example.noisecanseling.network.RetrofitClient
import com.example.noisecanseling.network.SaveResponse
import com.example.noisecanseling.network.TokenManager
import com.example.noisecanseling.network.UserProfileRequest
import kotlinx.coroutines.launch

// ── 마이페이지 진입점 ──────────────────────────────
@Composable
fun MyPageScreen(
    onSongClick: (Int) -> Unit = {},
    onLoginClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onArtistClick: (String) -> Unit = {}
) {
    var mode by remember { mutableStateOf(0) } // 0=유저, 1=아티스트

    Column(modifier = Modifier.fillMaxSize().background(AppBg)) {
        // 상단 헤더
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("마이페이지", color = AppText, fontSize = 22.sp, fontWeight = FontWeight.Black)
            }
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, contentDescription = "설정", tint = AppText)
            }
        }

        // 모드 탭
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(AppSurface),
        ) {
            listOf("유저", "아티스트").forEachIndexed { index, label ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (mode == index) AppPrimary else Color.Transparent)
                        .clickable { mode = index }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        color = if (mode == index) AppBg else AppSubText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        if (mode == 0) {
            UserModeContent(
                onLoginClick = onLoginClick,
                onSongClick = onSongClick,
                onArtistClick = onArtistClick
            )
        } else {
            ArtistModeContent(
                onLoginClick = onLoginClick,
                onSongClick = onSongClick
            )
        }
    }
}

// ── 유저 모드 ──────────────────────────────────────
@Composable
fun UserModeContent(
    onLoginClick: () -> Unit,
    onSongClick: (Int) -> Unit,
    onArtistClick: (String) -> Unit
) {
    var saves by remember { mutableStateOf<List<SaveResponse>>(emptyList()) }
    var ratings by remember { mutableStateOf<Map<Int, Int>>(emptyMap()) }
    var myComments by remember { mutableStateOf<List<com.example.noisecanseling.network.MyCommentItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) return@LaunchedEffect
        isLoading = true
        try {
            val savesRes = RetrofitClient.api.getMySaves(TokenManager.getBearerToken())
            if (savesRes.isSuccessful) {
                val loaded = savesRes.body() ?: emptyList()
                saves = loaded

                // fetch rating for each save
                val ratingMap = mutableMapOf<Int, Int>()
                loaded.forEach { save ->
                    try {
                        val r = RetrofitClient.api.getRating(TokenManager.getBearerToken(), save.song_id)
                        if (r.isSuccessful) ratingMap[save.song_id] = r.body()?.my_score ?: 0
                    } catch (e: Exception) { /* skip */ }
                }
                ratings = ratingMap

                // fetch comments for each save and filter by current user
                val nick = TokenManager.getNickname() ?: ""
                val commentList = mutableListOf<com.example.noisecanseling.network.MyCommentItem>()
                loaded.take(20).forEach { save ->
                    try {
                        val c = RetrofitClient.api.getComments(save.song_id)
                        if (c.isSuccessful) {
                            c.body()?.filter { it.nickname == nick }?.forEach { comment ->
                                commentList.add(
                                    com.example.noisecanseling.network.MyCommentItem(
                                        songId = save.song_id,
                                        blindSongTitle = blindTitle(save.song_id),
                                        commentId = comment.id,
                                        content = comment.content,
                                        createdAt = comment.created_at
                                    )
                                )
                            }
                        }
                    } catch (e: Exception) { /* skip */ }
                }
                myComments = commentList.sortedByDescending { it.createdAt }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    val followingArtists = saves.map { it.uploader }.distinct()
    val ratedSaves = saves.filter { (ratings[it.song_id] ?: 0) > 0 }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { UserProfileBlock(onLoginClick = onLoginClick) }

        // Following 섹션
        item {
            Text("Following", color = AppText, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = InterFontFamily)
            Spacer(Modifier.height(8.dp))
        }

        if (!isLoggedIn) {
            item { EmptyStateCard("로그인 후 팔로잉한 아티스트를 볼 수 있어요") }
        } else if (isLoading) {
            item {
                Box(Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppPrimary, modifier = Modifier.size(24.dp))
                }
            }
        } else if (followingArtists.isEmpty()) {
            item { EmptyStateCard("저장한 곡이 없어요. 곡을 저장하면 아티스트가 표시돼요.") }
        } else {
            items(followingArtists) { artistName ->
                val artistSongs = saves.filter { it.uploader == artistName }
                FollowingArtistRow(
                    artistName = artistName,
                    songCount = artistSongs.size,
                    latestSong = artistSongs.firstOrNull()?.original_title ?: "",
                    onClick = { onArtistClick(artistName) }
                )
            }
        }

        // My Ratings 섹션 — 가로 스크롤
        item {
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("My Ratings", color = AppText, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = InterFontFamily, modifier = Modifier.weight(1f))
                if (ratedSaves.size > 3) Text("${ratedSaves.size}개 →", color = AppSubText, fontSize = 12.sp, fontFamily = InterFontFamily)
            }
            Spacer(Modifier.height(8.dp))
            if (!isLoggedIn) {
                EmptyStateCard("로그인 후 평점 내역을 볼 수 있어요")
            } else if (ratedSaves.isEmpty()) {
                EmptyStateCard("평점을 남긴 곡이 없어요")
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(ratedSaves) { save ->
                        RatedSongCard(save = save, myScore = ratings[save.song_id] ?: 0, onPlayClick = { onSongClick(save.song_id) })
                    }
                }
            }
        }

        // Chat 섹션 — 가로 스크롤 (로컬 + 서버 병합)
        item {
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Chat", color = AppText, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = InterFontFamily, modifier = Modifier.weight(1f))
                val totalChats = myLocalComments.size + myComments.size
                if (totalChats > 3) Text("${totalChats}개 →", color = AppSubText, fontSize = 12.sp, fontFamily = InterFontFamily)
            }
            Spacer(Modifier.height(8.dp))
            if (!isLoggedIn) {
                EmptyStateCard("로그인 후 댓글 내역을 볼 수 있어요")
            } else {
                // 로컬(방금 쓴 댓글) + 서버 댓글 병합, 중복 제거
                val merged = (myLocalComments.map { lc ->
                    com.example.noisecanseling.network.MyCommentItem(lc.songId, lc.blindTitle, -1, lc.content, "")
                } + myComments).distinctBy { it.content + it.songId }
                if (merged.isEmpty()) {
                    EmptyStateCard("남긴 댓글이 없어요")
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(merged, key = { "${it.songId}_${it.commentId}_${it.content}" }) { item ->
                            MyCommentCard(item = item, onSongClick = { onSongClick(item.songId) })
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

// 가로 스크롤용 평점 카드 (width 160dp)
@Composable
fun RatedSongCard(save: SaveResponse, myScore: Int = 0, onPlayClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier.width(160.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            MiniCover(save.song_id)
            Spacer(Modifier.height(8.dp))
            Text(
                blindTitle(save.song_id),
                color = AppText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                fontFamily = InterFontFamily, maxLines = 1
            )
            Text(save.uploader, color = AppSubText, fontSize = 11.sp, fontFamily = InterFontFamily, maxLines = 1)
            Spacer(Modifier.height(6.dp))
            Row {
                repeat(5) { i ->
                    Icon(
                        if (i < myScore) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = null, tint = AppPrimary, modifier = Modifier.size(13.dp)
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier.size(28.dp).background(AppPrimary),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onPlayClick, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "재생", tint = AppBg, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// 가로 스크롤용 댓글 카드 (width 200dp)
@Composable
fun MyCommentCard(item: com.example.noisecanseling.network.MyCommentItem, onSongClick: () -> Unit = {}) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier.width(200.dp).clickable { onSongClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MiniCover(item.songId)
                Spacer(Modifier.width(8.dp))
                Text(
                    item.blindSongTitle,
                    color = AppSubText, fontSize = 11.sp,
                    fontFamily = InterFontFamily, maxLines = 1, modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "\"${item.content}\"",
                color = AppText, fontSize = 13.sp,
                fontFamily = InterFontFamily, maxLines = 3
            )
        }
    }
}

@Composable
fun MyCommentRow(item: com.example.noisecanseling.network.MyCommentItem, onSongClick: () -> Unit = {}) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().clickable { onSongClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MiniCover(item.songId)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.blindSongTitle,
                    color = AppSubText,
                    fontSize = 11.sp,
                    fontFamily = InterFontFamily
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "\"${item.content}\"",
                    color = AppText,
                    fontSize = 13.sp,
                    fontFamily = InterFontFamily,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
fun FollowingArtistRow(
    artistName: String,
    songCount: Int,
    latestSong: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 아티스트 아바타 (이니셜)
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(AppPrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    artistName.take(1).uppercase(),
                    color = AppPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(artistName, color = AppText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    if (latestSong.isNotEmpty()) "최근: $latestSong" else "저장된 곡 ${songCount}개",
                    color = AppSubText, fontSize = 12.sp, maxLines = 1
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AppSubText, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun RatedSongRow(save: SaveResponse, myScore: Int = 0, onPlayClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MiniCover(save.song_id)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    blindTitle(save.song_id),
                    color = AppText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = InterFontFamily
                )
                Text(save.uploader, color = AppSubText, fontSize = 12.sp, fontFamily = InterFontFamily)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(5) { i ->
                    Icon(
                        if (i < myScore) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = null,
                        tint = AppPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Spacer(Modifier.width(6.dp))
            IconButton(onClick = onPlayClick, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.PlayArrow, contentDescription = "재생", tint = AppPrimary, modifier = Modifier.size(20.dp))
            }
        }
    }
}

// ── 아티스트 모드 ──────────────────────────────────
@Composable
fun ArtistModeContent(onLoginClick: () -> Unit, onSongClick: (Int) -> Unit) {
    var artistSubTab by remember { mutableStateOf(0) } // 0=프로필, 1=수익

    Column(modifier = Modifier.fillMaxSize()) {
        // 프로필 / 수익 서브 탭
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(AppSurface),
        ) {
            listOf("프로필", "수익").forEachIndexed { index, label ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (artistSubTab == index) AppSurfaceHi else Color.Transparent)
                        .clickable { artistSubTab = index }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        color = if (artistSubTab == index) AppText else AppSubText,
                        fontWeight = if (artistSubTab == index) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        if (artistSubTab == 0) {
            ArtistProfileTab(onLoginClick = onLoginClick, onSongClick = onSongClick)
        } else {
            ArtistRevenueTab(onSongClick = onSongClick)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArtistProfileTab(onLoginClick: () -> Unit, onSongClick: (Int) -> Unit) {
    var showEditProfile by remember { mutableStateOf(false) }
    var debutDate by remember { mutableStateOf("") }
    var agency by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var artistType by remember { mutableStateOf("") }
    var photoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isSavingProfile by remember { mutableStateOf(false) }
    var saveProfileMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // 실시간 통계
    var uploadCount by remember { mutableStateOf<Int?>(null) }
    var totalStreaming by remember { mutableStateOf<Int?>(null) }
    var followerCount by remember { mutableStateOf<Int?>(null) }

    // 프로필 + 통계 불러오기
    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) return@LaunchedEffect
        try {
            val res = RetrofitClient.api.getMyProfile(TokenManager.getBearerToken())
            if (res.isSuccessful) {
                res.body()?.let { p ->
                    debutDate = p.debut_date ?: ""
                    artistType = p.artist_type ?: ""
                    agency = p.agency ?: ""
                    bio = p.bio ?: ""
                }
            }
        } catch (_: Exception) {}

        // 통계: 내 곡 목록으로 업로드 수 + 총 스트리밍 계산
        val nickname = globalNickname
        if (nickname.isNotEmpty()) {
            try {
                val songsRes = RetrofitClient.api.getSongs(page = 1, limit = 200)
                if (songsRes.isSuccessful) {
                    val mySongs = (songsRes.body() ?: emptyList()).filter { it.uploader == nickname }
                    uploadCount = mySongs.size
                    totalStreaming = mySongs.sumOf { it.play_count }
                }
            } catch (_: Exception) {}

            // 팔로워 수 (별도 stats API)
            try {
                val statsRes = RetrofitClient.api.getArtistStats(nickname)
                if (statsRes.isSuccessful) {
                    followerCount = statsRes.body()?.follower_count
                }
            } catch (_: Exception) {}
        }
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris -> photoUris = photoUris + uris }

    if (showEditProfile) {
        EditProfileDialog(
            onDismiss = { showEditProfile = false },
            onPhotoSelected = { uri ->
                photoUris = listOf(uri) + photoUris
                showEditProfile = false
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = AppSurface),
                shape = RoundedCornerShape(0.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // 프로필 사진 업로드 영역
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(AppSurfaceHi)
                                .clickable { photoPicker.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (photoUris.isNotEmpty()) {
                                AsyncImage(
                                    model = photoUris.first(),
                                    contentDescription = "프로필 사진",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(24.dp))
                                    Text("사진", color = AppSubText, fontSize = 10.sp, fontFamily = InterFontFamily)
                                }
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                if (isLoggedIn) globalNickname else "로그인 필요",
                                color = AppText, fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = InterFontFamily
                            )
                            Text("블라인드 크리에이터", color = AppSubText, fontSize = 13.sp, fontFamily = InterFontFamily)
                        }
                        IconButton(onClick = { if (isLoggedIn) showEditProfile = true else onLoginClick() }) {
                            Icon(Icons.Default.Edit, contentDescription = "프로필 편집", tint = AppPrimary)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = AppSurface.copy(alpha = 0.5f))
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ArtistStat("업로드", if (uploadCount != null) "${uploadCount}곡" else "—")
                        ArtistStat("총 스트리밍", if (totalStreaming != null) formatStatCount(totalStreaming!!) else "—")
                        ArtistStat("팔로워", if (followerCount != null) formatStatCount(followerCount!!) else "—")
                    }
                }
            }
        }

        // 아티스트 정보 편집
        item {
            Text("아티스트 정보", color = AppText, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = InterFontFamily)
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = AppSurface),
                shape = RoundedCornerShape(0.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(value = debutDate, onValueChange = { debutDate = it }, label = { Text("데뷔일") }, placeholder = { Text("예: 2020-01-01") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = profileFieldColors())
                    OutlinedTextField(value = artistType, onValueChange = { artistType = it }, label = { Text("활동 유형") }, placeholder = { Text("예: 솔로, 밴드, DJ ...") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = profileFieldColors())
                    OutlinedTextField(value = agency, onValueChange = { agency = it }, label = { Text("소속사") }, placeholder = { Text("없으면 '인디펜던트'") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = profileFieldColors())
                    OutlinedTextField(value = bio, onValueChange = { bio = it }, label = { Text("아티스트 소개") }, modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp), maxLines = 4, colors = profileFieldColors())
                    if (saveProfileMessage.isNotEmpty()) {
                        Text(saveProfileMessage, color = AppPrimary, fontSize = 12.sp, fontFamily = InterFontFamily)
                    }
                    Button(
                        onClick = {
                            if (!isLoggedIn) { onLoginClick(); return@Button }
                            isSavingProfile = true
                            saveProfileMessage = ""
                            scope.launch {
                                try {
                                    // Retrofit 서버 저장
                                    val res = RetrofitClient.api.saveProfile(
                                        token = TokenManager.getBearerToken(),
                                        body = UserProfileRequest(
                                            debut_date = debutDate,
                                            artist_type = artistType,
                                            agency = agency,
                                            bio = bio
                                        )
                                    )
                                    saveProfileMessage = if (res.isSuccessful) "저장 완료 ✓" else "저장 실패 (${res.code()})"
                                } catch (e: Exception) {
                                    saveProfileMessage = "오류: ${e.message}"
                                } finally {
                                    isSavingProfile = false
                                }
                            }
                        },
                        enabled = !isSavingProfile,
                        colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
                        shape = RoundedCornerShape(0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isSavingProfile) {
                            CircularProgressIndicator(color = AppBg, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text("저장", color = AppBg, fontWeight = FontWeight.Bold, fontFamily = InterFontFamily)
                        }
                    }
                }
            }
        }

        // 사진 갤러리
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("갤러리", color = AppText, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = InterFontFamily, modifier = Modifier.weight(1f))
                TextButton(onClick = { photoPicker.launch("image/*") }) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("사진 추가", color = AppPrimary, fontSize = 13.sp, fontFamily = InterFontFamily)
                }
            }
        }

        if (photoUris.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(AppSurface)
                        .clickable { photoPicker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = AppSubText, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.height(4.dp))
                        Text("사진을 업로드하세요", color = AppSubText, fontSize = 13.sp, fontFamily = InterFontFamily)
                    }
                }
            }
        } else {
            item {
                // LazyRow snap — 화면에 2개씩 노출, 스와이프로 더 보기
                val screenWidth = LocalConfiguration.current.screenWidthDp.dp
                val itemSize = (screenWidth - 40.dp - 4.dp) / 2  // 좌우 패딩 20dp씩 + 간격 4dp
                val listState = rememberLazyListState()
                val snapBehavior = rememberSnapFlingBehavior(lazyListState = listState)

                LazyRow(
                    state = listState,
                    flingBehavior = snapBehavior,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(photoUris) { uri ->
                        Box(
                            modifier = Modifier
                                .size(itemSize)
                        ) {
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            // 삭제 버튼
                            IconButton(
                                onClick = { photoUris = photoUris - uri },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(28.dp)
                                    .background(Color.Black.copy(alpha = 0.5f))
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "삭제", tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }

                // 페이지 인디케이터 (사진이 3개 이상일 때만 표시)
                if (photoUris.size > 2) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val firstVisible = listState.firstVisibleItemIndex
                        photoUris.indices.forEach { i ->
                            Box(
                                modifier = Modifier
                                    .size(if (i == firstVisible || i == firstVisible + 1) 6.dp else 4.dp)
                                    .background(
                                        if (i == firstVisible || i == firstVisible + 1)
                                            AppPrimary else AppSubText.copy(alpha = 0.4f)
                                    )
                            )
                            if (i < photoUris.lastIndex) Spacer(Modifier.width(4.dp))
                        }
                    }
                }
            }
        }

        // 내가 올린 음악 + 삭제 버튼
        item {
            Text("내가 올린 음악", color = AppText, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = InterFontFamily)
        }
        itemsWithDelete(sampleChartSongs.take(4), onSongClick = onSongClick, scope = scope)

        item { Spacer(Modifier.height(24.dp)) }
    }
}

// Extension function to add items with delete button inline
private fun androidx.compose.foundation.lazy.LazyListScope.itemsWithDelete(
    songs: List<ChartSong>,
    onSongClick: (Int) -> Unit,
    scope: kotlinx.coroutines.CoroutineScope
) {
    songs.forEachIndexed { index, song ->
        item(key = song.id) {
            var showDeleteConfirm by remember { mutableStateOf(false) }
            var isDeleting by remember { mutableStateOf(false) }

            if (showDeleteConfirm) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirm = false },
                    containerColor = AppSurface,
                    title = { Text("음원 삭제", color = AppText, fontWeight = FontWeight.Bold, fontFamily = InterFontFamily) },
                    text = { Text("이 음원을 삭제하시겠습니까?\n삭제된 음원은 복구할 수 없습니다.", color = AppSubText, fontFamily = InterFontFamily) },
                    confirmButton = {
                        TextButton(onClick = {
                            showDeleteConfirm = false
                            isDeleting = true
                            scope.launch {
                                try {
                                    RetrofitClient.api.deleteSong(TokenManager.getBearerToken(), song.id)
                                } catch (e: Exception) { e.printStackTrace() } finally { isDeleting = false }
                            }
                        }) { Text("삭제", color = Color(0xFFFF6B6B), fontWeight = FontWeight.Bold, fontFamily = InterFontFamily) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirm = false }) { Text("취소", color = AppSubText, fontFamily = InterFontFamily) }
                    }
                )
            }

            SongRowFrame {
                Text("${index + 1}", color = AppPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp))
                MiniCover(song.id)
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(song.title, color = AppText, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = InterFontFamily)
                    Text(song.blindName, color = AppSubText, fontSize = 11.sp, fontFamily = InterFontFamily)
                }
                if (isDeleting) {
                    CircularProgressIndicator(color = AppPrimary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    IconButton(onClick = { onSongClick(song.id % songList.size) }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "재생", tint = AppPrimary, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "삭제", tint = Color(0xFFFF6B6B), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ArtistRevenueTab(onSongClick: (Int) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { SettlementCard() }

        item {
            Text("평균 평점 현황", color = AppText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        item { RatingDashboard() }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
fun ArtistStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = AppPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(label, color = AppSubText, fontSize = 12.sp)
    }
}

fun formatStatCount(count: Int): String = when {
    count >= 1_000_000 -> "%.1fM".format(count / 1_000_000f)
    count >= 1_000 -> "%.1fK".format(count / 1_000f)
    else -> "$count"
}

@Composable
fun RatingDashboard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            sampleChartSongs.take(4).forEach { song ->
                val rating = (3..5).random().toFloat()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(song.blindName, color = AppSubText, fontSize = 12.sp, modifier = Modifier.width(110.dp))
                    Spacer(Modifier.width(8.dp))
                    LinearProgressIndicator(
                        progress = { rating / 5f },
                        modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = AppPrimary,
                        trackColor = AppSurface.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("%.1f".format(rating), color = AppPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.Star, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

// EditProfileDialog — 사진만 변경 (닉네임/소개글 제거)
@Composable
fun EditProfileDialog(
    onDismiss: () -> Unit,
    onPhotoSelected: (Uri) -> Unit = {}
) {
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            onPhotoSelected(uri)
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppSurface,
        title = { Text("프로필 사진 변경", color = AppText, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Default.AddAPhoto,
                    contentDescription = null,
                    tint = AppPrimary,
                    modifier = Modifier.size(56.dp)
                )
                Text(
                    "갤러리에서 사진을 선택하면\n마이페이지에 즉시 반영됩니다.",
                    color = AppSubText,
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    fontFamily = InterFontFamily
                )
                Button(
                    onClick = { photoPicker.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Photo, contentDescription = null,
                        tint = AppBg, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("사진 선택", color = AppBg, fontWeight = FontWeight.Bold, fontFamily = InterFontFamily)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소", color = AppSubText) }
        }
    )
}

@Composable
private fun profileFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AppPrimary,
    unfocusedBorderColor = AppSubText,
    focusedLabelColor = AppPrimary,
    unfocusedLabelColor = AppSubText,
    cursorColor = AppPrimary,
    focusedTextColor = AppText,
    unfocusedTextColor = AppText
)

@Composable
fun EmptyStateCard(message: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(message, color = AppSubText, fontSize = 13.sp)
        }
    }
}

// ── 공통 컴포넌트 ──────────────────────────────────
@Composable
fun UserProfileBlock(onLoginClick: () -> Unit = {}) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        shape = RoundedCornerShape(18.dp),
        modifier = if (!isLoggedIn) Modifier.clickable { onLoginClick() } else Modifier
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Avatar()
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    if (isLoggedIn) globalNickname else "로그인이 필요합니다",
                    color = AppText, fontSize = 20.sp, fontWeight = FontWeight.Bold
                )
                Text(
                    if (isLoggedIn) "블라인드 리스너" else "탭하여 로그인하기",
                    color = AppSubText, fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun SettlementCard() {
    val mySongs = sampleChartSongs.take(4)
    val totalStreams = mySongs.sumOf { song ->
        val base = song.totalStreams.replace(",", "").toIntOrNull() ?: 0
        val extra = songStreamCounts[song.id % songList.size] ?: 0
        base + extra
    }
    val dailyStreams = mySongs.sumOf { song ->
        val base = song.dailyStreams.replace(",", "").toIntOrNull() ?: 0
        val extra = songStreamCounts[song.id % songList.size] ?: 0
        base + extra
    }

    var showWithdrawSheet by remember { mutableStateOf(false) }

    if (showWithdrawSheet) {
        WithdrawBottomSheet(onDismiss = { showWithdrawSheet = false })
    }

    Card(colors = CardDefaults.cardColors(containerColor = AppSurface), shape = RoundedCornerShape(18.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("정산 가능 금액", color = AppSubText, fontSize = 13.sp)
                    Text("500,000 won", color = AppPrimary, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { showWithdrawSheet = true },
                    colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("출금하기", color = AppBg, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Metric("토탈 스트리밍 수", "%,d".format(totalStreams))
                Metric("하루 스트리밍 수", "%,d".format(dailyStreams))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawBottomSheet(onDismiss: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    var bankName by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var enteredCode by remember { mutableStateOf("") }
    var codeSent by remember { mutableStateOf(false) }
    var codeVerified by remember { mutableStateOf(false) }
    var codeError by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = AppSurface) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (step) {
                0 -> {
                    Text("출금 계좌 등록", color = AppText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("계좌 정보와 본인 전화번호를 입력해주세요.", color = AppSubText, fontSize = 13.sp)
                    OutlinedTextField(value = bankName, onValueChange = { bankName = it }, label = { Text("은행명") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = withdrawFieldColors())
                    OutlinedTextField(value = accountNumber, onValueChange = { accountNumber = it.filter { c -> c.isDigit() || c == '-' } }, label = { Text("계좌번호") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), colors = withdrawFieldColors())
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(value = phoneNumber, onValueChange = { phoneNumber = it.filter { c -> c.isDigit() }; codeSent = false; codeVerified = false; enteredCode = "" }, label = { Text("휴대폰 번호") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.weight(1f), colors = withdrawFieldColors())
                        Button(onClick = { codeSent = true; codeVerified = false; codeError = false; enteredCode = "" }, enabled = phoneNumber.length >= 10, colors = ButtonDefaults.buttonColors(containerColor = AppSurfaceHi, disabledContainerColor = AppSurfaceHi.copy(alpha = 0.4f)), shape = RoundedCornerShape(10.dp), modifier = Modifier.height(56.dp).align(Alignment.CenterVertically)) {
                            Text(if (codeSent) "재전송" else "인증번호 발송", color = AppPrimary, fontSize = 12.sp)
                        }
                    }
                    if (codeSent) {
                        Text("인증번호가 발송되었습니다. (테스트: 123456)", color = AppPrimary, fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(value = enteredCode, onValueChange = { enteredCode = it; codeError = false; codeVerified = false }, label = { Text("인증번호 6자리") }, singleLine = true, isError = codeError, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f), colors = withdrawFieldColors())
                            Button(onClick = { if (enteredCode == "123456") { codeVerified = true; codeError = false } else { codeVerified = false; codeError = true } }, enabled = enteredCode.isNotBlank(), colors = ButtonDefaults.buttonColors(containerColor = if (codeVerified) AppGreen else AppSurfaceHi, disabledContainerColor = AppSurfaceHi.copy(alpha = 0.4f)), shape = RoundedCornerShape(10.dp), modifier = Modifier.height(56.dp).align(Alignment.CenterVertically)) {
                                Text(if (codeVerified) "인증 완료" else "인증 확인", color = AppText, fontSize = 12.sp)
                            }
                        }
                        if (codeError) Text("인증번호가 올바르지 않습니다.", color = Color(0xFFFF6B6B), fontSize = 12.sp)
                        if (codeVerified) Text("본인 인증이 완료되었습니다.", color = AppGreen, fontSize = 12.sp)
                    }
                    Button(onClick = { step = 1 }, enabled = bankName.isNotBlank() && accountNumber.isNotBlank() && codeVerified, colors = ButtonDefaults.buttonColors(containerColor = AppPrimary, disabledContainerColor = AppSurfaceHi), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(52.dp)) {
                        Text("다음", color = AppBg, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
                1 -> {
                    Text("출금 신청", color = AppText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("아래 정보로 출금을 신청합니다.", color = AppSubText, fontSize = 13.sp)
                    Card(colors = CardDefaults.cardColors(containerColor = AppBg), shape = RoundedCornerShape(12.dp)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            WithdrawInfoRow("은행", bankName)
                            WithdrawInfoRow("계좌번호", accountNumber)
                            WithdrawInfoRow("휴대폰", phoneNumber)
                            WithdrawInfoRow("출금 금액", "500,000 won")
                        }
                    }
                    Button(onClick = { step = 2 }, colors = ButtonDefaults.buttonColors(containerColor = AppPrimary), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(52.dp)) {
                        Text("출금 신청", color = AppBg, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    OutlinedButton(onClick = { step = 0 }, colors = ButtonDefaults.outlinedButtonColors(contentColor = AppSubText), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(52.dp)) {
                        Text("이전", fontSize = 16.sp)
                    }
                }
                2 -> {
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(72.dp))
                        Text("출금 신청 완료!", color = AppText, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text("1~3 영업일 내로 입금됩니다.", color = AppSubText, fontSize = 14.sp)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = AppPrimary), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(52.dp)) {
                            Text("확인", color = AppBg, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WithdrawInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = AppSubText, fontSize = 14.sp)
        Text(value, color = AppText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun withdrawFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AppPrimary,
    unfocusedBorderColor = AppSubText,
    focusedLabelColor = AppPrimary,
    unfocusedLabelColor = AppSubText,
    cursorColor = AppPrimary,
    focusedTextColor = AppText,
    unfocusedTextColor = AppText
)

@Composable
fun Metric(label: String, value: String) {
    Column {
        Text(label, color = AppSubText, fontSize = 12.sp)
        Text(value, color = AppText, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MySongRow(rank: Int, song: ChartSong, onPlayClick: () -> Unit = {}) {
    val songIndex = song.id % songList.size
    val extraStreams = songStreamCounts[songIndex] ?: 0
    val baseTotalStreams = song.totalStreams.replace(",", "").toIntOrNull() ?: 0
    val baseDailyStreams = song.dailyStreams.replace(",", "").toIntOrNull() ?: 0
    val displayTotal = "%,d".format(baseTotalStreams + extraStreams)
    val displayDaily = "%,d".format(baseDailyStreams + extraStreams)

    SongRowFrame {
        Text("$rank", color = AppPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp))
        MiniCover(song.id)
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(song.title, color = AppText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("${song.blindName} · total $displayTotal · today $displayDaily", color = AppSubText, fontSize = 11.sp, maxLines = 1)
        }
        RowActions(onPlayClick = onPlayClick)
    }
}
