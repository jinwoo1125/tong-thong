package com.example.noisecanseling

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MyPageScreen(onSongClick: (Int) -> Unit = {}, onLoginClick: () -> Unit = {}, onSettingsClick: () -> Unit = {}) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(AppBg).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Default.Settings, contentDescription = "설정", tint = AppText)
                }
            }
            UserProfileBlock(onLoginClick = onLoginClick)
        }
        item { SettlementCard() }
        item { SectionTitle("내가 올린 음악 순위") }
        items(sampleChartSongs.take(4)) { song ->
            MySongRow(
                rank = sampleChartSongs.indexOf(song) + 1,
                song = song,
                onPlayClick = { onSongClick(song.id % songList.size) }
            )
        }
    }
}

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
                    if (isLoggedIn) (com.example.noisecanseling.network.TokenManager.getNickname() ?: "유저 이름") else "로그인이 필요합니다",
                    color = AppText, fontSize = 20.sp, fontWeight = FontWeight.Bold
                )
                Text(
                    if (isLoggedIn) "블라인드 크리에이터" else "탭하여 로그인하기",
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
    // 단계: 0=계좌입력, 1=인증확인, 2=신청완료
    var step by remember { mutableIntStateOf(0) }
    var bankName by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var enteredCode by remember { mutableStateOf("") }
    var codeSent by remember { mutableStateOf(false) }
    var codeVerified by remember { mutableStateOf(false) }
    var codeError by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (step) {
                // ── 1단계: 계좌 + 전화번호 인증 ──
                0 -> {
                    Text("출금 계좌 등록", color = AppText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("계좌 정보와 본인 전화번호를 입력해주세요.", color = AppSubText, fontSize = 13.sp)

                    OutlinedTextField(
                        value = bankName,
                        onValueChange = { bankName = it },
                        label = { Text("은행명 (예: 카카오뱅크)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = withdrawFieldColors()
                    )
                    OutlinedTextField(
                        value = accountNumber,
                        onValueChange = { accountNumber = it.filter { c -> c.isDigit() || c == '-' } },
                        label = { Text("계좌번호") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = withdrawFieldColors()
                    )

                    // 전화번호 + 인증번호 발송
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = {
                                phoneNumber = it.filter { c -> c.isDigit() }
                                codeSent = false
                                codeVerified = false
                                enteredCode = ""
                            },
                            label = { Text("휴대폰 번호") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.weight(1f),
                            colors = withdrawFieldColors()
                        )
                        Button(
                            onClick = {
                                codeSent = true
                                codeVerified = false
                                codeError = false
                                enteredCode = ""
                            },
                            enabled = phoneNumber.length >= 10,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppSurfaceHi,
                                disabledContainerColor = AppSurfaceHi.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(56.dp).align(Alignment.CenterVertically)
                        ) {
                            Text(if (codeSent) "재전송" else "인증번호 발송", color = AppPrimary, fontSize = 12.sp)
                        }
                    }

                    // 인증번호 입력 + 인증 확인 버튼 (발송 후 표시)
                    if (codeSent) {
                        Text(
                            "인증번호가 발송되었습니다. (테스트: 123456)",
                            color = AppPrimary, fontSize = 12.sp
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = enteredCode,
                                onValueChange = {
                                    enteredCode = it
                                    codeError = false
                                    codeVerified = false
                                },
                                label = { Text("인증번호 6자리") },
                                singleLine = true,
                                isError = codeError,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                colors = withdrawFieldColors()
                            )
                            Button(
                                onClick = {
                                    if (enteredCode == "123456") {
                                        codeVerified = true
                                        codeError = false
                                    } else {
                                        codeVerified = false
                                        codeError = true
                                    }
                                },
                                enabled = enteredCode.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (codeVerified) AppGreen else AppSurfaceHi,
                                    disabledContainerColor = AppSurfaceHi.copy(alpha = 0.4f)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(56.dp).align(Alignment.CenterVertically)
                            ) {
                                Text(
                                    if (codeVerified) "인증 완료" else "인증 확인",
                                    color = AppText, fontSize = 12.sp
                                )
                            }
                        }
                        if (codeError) {
                            Text("인증번호가 올바르지 않습니다.", color = Color(0xFFFF6B6B), fontSize = 12.sp)
                        }
                        if (codeVerified) {
                            Text("본인 인증이 완료되었습니다.", color = AppGreen, fontSize = 12.sp)
                        }
                    }

                    Button(
                        onClick = { step = 1 },
                        enabled = bankName.isNotBlank() && accountNumber.isNotBlank() && codeVerified,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppPrimary,
                            disabledContainerColor = AppSurfaceHi
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Text("다음", color = AppBg, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                // ── 2단계: 출금 신청 확인 ──
                1 -> {
                    Text("출금 신청", color = AppText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("아래 정보로 출금을 신청합니다.", color = AppSubText, fontSize = 13.sp)

                    Card(
                        colors = CardDefaults.cardColors(containerColor = AppBg),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            WithdrawInfoRow("은행", bankName)
                            WithdrawInfoRow("계좌번호", accountNumber)
                            WithdrawInfoRow("휴대폰", phoneNumber)
                            WithdrawInfoRow("출금 금액", "500,000 won")
                        }
                    }

                    Button(
                        onClick = { step = 2 },
                        colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Text("출금 신청", color = AppBg, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    OutlinedButton(
                        onClick = { step = 0 },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppSubText),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Text("이전", fontSize = 16.sp)
                    }
                }

                // ── 3단계: 완료 ──
                2 -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = AppPrimary,
                            modifier = Modifier.size(72.dp)
                        )
                        Text("출금 신청 완료!", color = AppText, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text("1~3 영업일 내로 입금됩니다.", color = AppSubText, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) {
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
            Text("${song.blindName} · total $displayTotal · today $displayDaily",
                color = AppSubText, fontSize = 11.sp, maxLines = 1)
        }
        RowActions(onPlayClick = onPlayClick)
    }
}
