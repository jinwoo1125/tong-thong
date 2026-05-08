package com.example.noisecanseling

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val UP_BG = Color(0xFF0E1117)
private val UP_SURFACE = Color(0xFF171B24)
private val UP_PRIMARY = Color(0xFFFFC857)
private val UP_TEXT = Color.White
private val UP_SUBTEXT = Color(0xFF9BA3B4)

data class UploadData(
    val audioUri: Uri,
    val audioFileName: String,
    val coverUri: Uri?,
    val title: String,
    val genreId: Int?,        // primary genre (first selected)
    val genreIds: List<Int>,  // all selected genre IDs (up to 3)
    val description: String,
    val lyrics: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    onBackClick: () -> Unit = {},
    onUploadClick: (UploadData) -> Unit = {}
) {
    val context = LocalContext.current
    val genreList = listOf("팝", "힙합", "R&B", "록", "재즈", "클래식", "일렉트로닉", "인디", "발라드", "트로트", "OST", "기타")
    val genreIdMap = mapOf("팝" to 1, "힙합" to 2, "R&B" to 3, "록" to 4, "재즈" to 5, "클래식" to 6, "일렉트로닉" to 7, "인디" to 8, "발라드" to 9, "트로트" to 10, "OST" to 11, "기타" to 12)

    var audioUri by remember { mutableStateOf<Uri?>(null) }
    var audioFileName by remember { mutableStateOf("") }
    var coverUri by remember { mutableStateOf<Uri?>(null) }
    var titleText by remember { mutableStateOf("") }
    var selectedGenres by remember { mutableStateOf<List<String>>(emptyList()) }
    var customGenreText by remember { mutableStateOf("") }
    var descriptionText by remember { mutableStateOf("") }
    var lyricsText by remember { mutableStateOf("") }
    var showGenreWarning by remember { mutableStateOf(false) }
    val maxGenres = 3

    val audioLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            audioUri = uri
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val idx = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0) {
                        audioFileName = it.getString(idx)
                        if (titleText.isBlank()) titleText = audioFileName.substringBeforeLast(".")
                    }
                }
            }
        }
    }

    val coverLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) coverUri = uri
    }

    Scaffold(
        containerColor = UP_BG,
        topBar = {
            TopAppBar(
                title = { Text("업로드", color = UP_TEXT) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기", tint = UP_TEXT)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = UP_BG)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(UP_BG)
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))

            // 음원 파일 선택
            SectionLabel("음원 파일 *")
            Spacer(Modifier.height(8.dp))
            FilePickerBox(
                icon = Icons.Default.MusicNote,
                label = if (audioUri != null) audioFileName else "음원 파일을 선택해주세요",
                hint = if (audioUri != null) "탭해서 다시 선택" else "tap to browse · mp3, wav, flac",
                selected = audioUri != null,
                onClick = { audioLauncher.launch("audio/*") }
            )

            Spacer(Modifier.height(16.dp))

            // 표지 이미지 선택
            SectionLabel("앨범 표지 (선택)")
            Spacer(Modifier.height(8.dp))
            FilePickerBox(
                icon = Icons.Default.Image,
                label = if (coverUri != null) "표지 이미지 선택됨" else "표지 이미지를 선택해주세요",
                hint = if (coverUri != null) "탭해서 다시 선택" else "tap to browse · jpg, png",
                selected = coverUri != null,
                onClick = { coverLauncher.launch("image/*") }
            )

            Spacer(Modifier.height(20.dp))

            // 곡 제목
            SectionLabel("곡 제목 *")
            Spacer(Modifier.height(8.dp))
            UpTextField(
                value = titleText,
                onValueChange = { titleText = it },
                placeholder = "곡 제목을 입력해주세요",
                singleLine = true
            )

            Spacer(Modifier.height(20.dp))

            // 장르 선택 (최대 3개)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                SectionLabel("장르 선택")
                Text(
                    "${selectedGenres.size}/$maxGenres 선택",
                    fontSize = 12.sp,
                    color = if (selectedGenres.size == maxGenres) UP_PRIMARY else UP_SUBTEXT
                )
            }
            Spacer(Modifier.height(8.dp))
            androidx.compose.foundation.layout.FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                genreList.forEach { genre ->
                    val selected = selectedGenres.contains(genre)
                    val disabled = !selected && selectedGenres.size >= maxGenres
                    FilterChip(
                        selected = selected,
                        onClick = {
                            if (selected) {
                                selectedGenres = selectedGenres - genre
                                if (genre == "기타") customGenreText = ""
                            } else if (!disabled) {
                                selectedGenres = selectedGenres + genre
                            }
                            showGenreWarning = false
                        },
                        label = {
                            Text(
                                genre,
                                color = when {
                                    selected -> UP_BG
                                    disabled -> UP_SUBTEXT.copy(alpha = 0.4f)
                                    else -> UP_TEXT
                                }
                            )
                        },
                        shape = RoundedCornerShape(0.dp),
                        enabled = !disabled || selected,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = UP_PRIMARY,
                            containerColor = UP_SURFACE,
                            selectedLabelColor = UP_BG,
                            labelColor = UP_TEXT,
                            disabledContainerColor = UP_SURFACE.copy(alpha = 0.5f),
                            disabledLabelColor = UP_SUBTEXT.copy(alpha = 0.4f)
                        )
                    )
                }
            }
            // '기타' 선택 시 직접 입력창 활성화
            if (selectedGenres.contains("기타")) {
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = customGenreText,
                    onValueChange = { customGenreText = it },
                    placeholder = { Text("기타 장르를 직접 입력해주세요", color = UP_SUBTEXT) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = UP_PRIMARY,
                        unfocusedBorderColor = UP_SUBTEXT.copy(alpha = 0.4f),
                        focusedTextColor = UP_TEXT,
                        unfocusedTextColor = UP_TEXT,
                        cursorColor = UP_PRIMARY,
                        focusedContainerColor = UP_SURFACE,
                        unfocusedContainerColor = UP_SURFACE
                    ),
                    shape = RoundedCornerShape(0.dp),
                    label = { Text("기타 장르명", color = UP_SUBTEXT) }
                )
            }

            Spacer(Modifier.height(20.dp))

            // 곡 소개
            SectionLabel("곡 소개")
            Spacer(Modifier.height(8.dp))
            UpTextField(
                value = descriptionText,
                onValueChange = { descriptionText = it },
                placeholder = "곡을 소개하는 짧은 글을 적어주세요",
                minHeight = 100,
                maxLines = 5
            )

            Spacer(Modifier.height(20.dp))

            // 가사
            SectionLabel("가사 (선택)")
            Spacer(Modifier.height(8.dp))
            UpTextField(
                value = lyricsText,
                onValueChange = { lyricsText = it },
                placeholder = "가사를 입력해주세요",
                minHeight = 200
            )

            Spacer(Modifier.height(32.dp))

            // 업로드 완료 버튼
            Button(
                onClick = {
                    val uri = audioUri ?: return@Button
                    val ids = selectedGenres.mapNotNull { genreIdMap[it] }
                    onUploadClick(
                        UploadData(
                            audioUri = uri,
                            audioFileName = audioFileName,
                            coverUri = coverUri,
                            title = titleText.ifBlank { audioFileName },
                            genreId = ids.firstOrNull(),
                            genreIds = ids,
                            description = descriptionText,
                            lyrics = lyricsText
                        )
                    )
                },
                enabled = audioUri != null && titleText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = UP_PRIMARY,
                    disabledContainerColor = UP_PRIMARY.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Upload, contentDescription = null, tint = UP_BG, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("업로드 완료", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = UP_BG)
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = UP_TEXT)
}

@Composable
private fun FilePickerBox(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    hint: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(UP_SURFACE)
            .border(
                width = 1.5.dp,
                color = if (selected) UP_PRIMARY else UP_SUBTEXT.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = if (selected) UP_PRIMARY else UP_SUBTEXT, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(label, fontSize = 14.sp, color = if (selected) UP_TEXT else UP_SUBTEXT, fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal)
                Text(hint, fontSize = 11.sp, color = UP_SUBTEXT)
            }
            if (selected) {
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = UP_PRIMARY, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun UpTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean = false,
    minHeight: Int = 56,
    maxLines: Int = Int.MAX_VALUE
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = UP_SUBTEXT) },
        singleLine = singleLine,
        maxLines = maxLines,
        modifier = Modifier.fillMaxWidth().heightIn(min = minHeight.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = UP_PRIMARY,
            unfocusedBorderColor = UP_SUBTEXT.copy(alpha = 0.4f),
            focusedTextColor = UP_TEXT,
            unfocusedTextColor = UP_TEXT,
            cursorColor = UP_PRIMARY,
            focusedContainerColor = UP_SURFACE,
            unfocusedContainerColor = UP_SURFACE
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun UploadCompleteScreen(
    onGoMainClick: () -> Unit = {},
    onGoMusicClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize().background(UP_BG).padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(96.dp), tint = UP_PRIMARY)
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "업로드 완료!", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = UP_TEXT)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "음원이 성공적으로 업로드되었습니다", fontSize = 14.sp, color = UP_SUBTEXT)
        Spacer(modifier = Modifier.height(56.dp))
        Button(
            onClick = onGoMusicClick,
            colors = ButtonDefaults.buttonColors(containerColor = UP_PRIMARY),
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("음악으로 가기", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = UP_BG)
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onGoMainClick,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = UP_PRIMARY),
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("메인으로", fontSize = 16.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UploadScreenPreview() {
    MaterialTheme { UploadScreen() }
}

@Preview(showBackground = true)
@Composable
fun UploadCompleteScreenPreview() {
    MaterialTheme { UploadCompleteScreen() }
}
