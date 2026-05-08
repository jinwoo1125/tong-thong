package com.example.noisecanseling

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadInitialScreen(
    onBackClick: () -> Unit = {},
    onFilePickClick: () -> Unit = {},
    onUploadClick: (Uri, String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
            // 파일명 추출
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) selectedFileName = it.getString(nameIndex)
                }
            }
        }
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
            modifier = Modifier.fillMaxSize().background(UP_BG).padding(innerPadding).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Surface(
                modifier = Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(16.dp)).clickable { launcher.launch("audio/*") },
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(width = 2.dp, color = UP_PRIMARY.copy(alpha = 0.4f)),
                color = UP_SURFACE.copy(alpha = 0.4f)
            ) {
                Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    if (selectedUri != null) {
                        Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(64.dp), tint = UP_PRIMARY)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = selectedFileName, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = UP_TEXT)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "탭하면 다시 선택할 수 있어요", fontSize = 12.sp, color = UP_SUBTEXT)
                    } else {
                        Icon(imageVector = Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(64.dp), tint = UP_PRIMARY.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "파일을 올려주세요", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = UP_TEXT)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "tap to browse", fontSize = 13.sp, color = UP_SUBTEXT)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { selectedUri?.let { onUploadClick(it, selectedFileName) } },
                enabled = selectedUri != null,
                colors = ButtonDefaults.buttonColors(containerColor = UP_PRIMARY, disabledContainerColor = UP_PRIMARY.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("업로드", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = UP_BG)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadDetailScreen(
    fileName: String = "노래제목.mp3",
    duration: String = "3:45",
    onBackClick: () -> Unit = {},
    onDeleteFile: () -> Unit = {},
    onUploadClick: (title: String, genreId: Int?) -> Unit = { _, _ -> }
) {
    val genreList = listOf("팝", "힙합", "R&B", "록", "재즈", "클래식", "일렉트로닉", "인디", "발라드", "트로트", "OST", "기타")
    val genreIdMap = mapOf("팝" to 1, "힙합" to 2, "R&B" to 3, "록" to 4, "재즈" to 5, "클래식" to 6, "일렉트로닉" to 7, "인디" to 8, "발라드" to 9, "트로트" to 10, "OST" to 11, "기타" to 12)
    var titleText by remember { mutableStateOf(fileName.substringBeforeLast(".")) }
    var selectedGenres by remember { mutableStateOf(setOf<String>()) }
    var showLimitWarning by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = UP_BG,
        topBar = {
            TopAppBar(
                title = { Text("업로드", color = UP_TEXT) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기", tint = UP_TEXT) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = UP_BG)
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().background(UP_BG).padding(innerPadding).padding(horizontal = 24.dp)) {
            Spacer(modifier = Modifier.height(24.dp))

            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = UP_SURFACE) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = UP_PRIMARY, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = fileName, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = UP_TEXT)
                        Text(text = duration, fontSize = 12.sp, color = UP_SUBTEXT)
                    }
                    IconButton(onClick = onDeleteFile) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "삭제", tint = UP_SUBTEXT)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = titleText,
                onValueChange = { titleText = it },
                label = { Text("곡 제목", color = UP_SUBTEXT) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = UP_PRIMARY,
                    unfocusedBorderColor = UP_SUBTEXT,
                    focusedTextColor = UP_TEXT,
                    unfocusedTextColor = UP_TEXT,
                    cursorColor = UP_PRIMARY
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "장르 선택", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = UP_TEXT)
                Text(text = "최대 3개 선택", fontSize = 12.sp, color = UP_SUBTEXT)
            }

            Spacer(modifier = Modifier.height(12.dp))

            androidx.compose.foundation.layout.FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                genreList.forEach { genre ->
                    val selected = genre in selectedGenres
                    FilterChip(
                        selected = selected,
                        onClick = {
                            if (selected) { selectedGenres = selectedGenres - genre; showLimitWarning = false }
                            else if (selectedGenres.size < 3) { selectedGenres = selectedGenres + genre; showLimitWarning = false }
                            else showLimitWarning = true
                        },
                        label = { Text(genre, color = if (selected) UP_BG else UP_TEXT) },
                        shape = RoundedCornerShape(50),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = UP_PRIMARY,
                            containerColor = UP_SURFACE,
                            selectedLabelColor = UP_BG,
                            labelColor = UP_TEXT
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (showLimitWarning) {
                Text(text = "3개까지만 선택이 가능합니다", fontSize = 13.sp, color = Color(0xFFFF6B6B))
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val genreId = selectedGenres.firstOrNull()?.let { genreIdMap[it] }
                    onUploadClick(titleText.ifBlank { fileName }, genreId)
                },
                enabled = titleText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = UP_PRIMARY, disabledContainerColor = UP_PRIMARY.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("업로드", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = UP_BG)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
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

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "업로드 1 - 시작")
@Composable
fun UploadInitialScreenPreview() {
    MaterialTheme { UploadInitialScreen() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "업로드 2 - 파일 정보 & 장르")
@Composable
fun UploadDetailScreenPreview() {
    MaterialTheme { UploadDetailScreen() }
}

@Preview(showBackground = true, name = "업로드 3 - 완료")
@Composable
fun UploadCompleteScreenPreview() {
    MaterialTheme { UploadCompleteScreen() }
}
