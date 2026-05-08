package com.example.noisecanseling

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import com.example.noisecanseling.network.TokenManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {},
    onLogout: () -> Unit = {},
    onLoginRequired: () -> Unit = {}
) {
    val context = LocalContext.current

    // 상태들
    var streamingQuality by remember { mutableStateOf("고음질") }
    var darkMode by remember {
        mutableStateOf(
            when (appThemeMode) {
                ThemeMode.DARK   -> "항상 다크 모드"
                ThemeMode.LIGHT  -> "항상 라이트 모드"
                ThemeMode.SYSTEM -> "시스템 설정"
            }
        )
    }
    var pushNotification by remember { mutableStateOf(true) }
    var selectedEq by remember { mutableStateOf("기본") }

    // 프로필 상태 (전역과 동기화)
    var editNickname by remember { mutableStateOf("") }
    var editAvatar by remember { mutableStateOf(0) }

    // 다이얼로그 표시 여부
    var showProfileEditDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var showQualityDialog by remember { mutableStateOf(false) }
    var showDarkModeDialog by remember { mutableStateOf(false) }
    var showEqDialog by remember { mutableStateOf(false) }
    var showLicenseDialog by remember { mutableStateOf(false) }
    var showVersionDialog by remember { mutableStateOf(false) }

    // ── 다이얼로그들 ──────────────────────────────

    if (showProfileEditDialog) {
        AlertDialog(
            onDismissRequest = { showProfileEditDialog = false },
            containerColor = AppSurface,
            title = { Text("프로필 수정", color = AppText, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // 현재 선택 미리보기
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        AvatarCircle(index = editAvatar, size = 72)
                    }
                    Text("프로필 선택", color = AppSubText, fontSize = 12.sp)
                    // 5x2 그리드
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0..4, 5..9).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                row.forEach { i ->
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .clickable { editAvatar = i }
                                            .then(
                                                if (editAvatar == i)
                                                    Modifier.border(3.dp, AppPrimary, CircleShape)
                                                else Modifier
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AvatarCircle(index = i, size = 52)
                                    }
                                }
                            }
                        }
                    }
                    OutlinedTextField(
                        value = editNickname,
                        onValueChange = { editNickname = it },
                        label = { Text("닉네임") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppPrimary,
                            unfocusedBorderColor = AppSubText,
                            focusedLabelColor = AppPrimary,
                            unfocusedLabelColor = AppSubText,
                            cursorColor = AppPrimary,
                            focusedTextColor = AppText,
                            unfocusedTextColor = AppText
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (editNickname.isNotBlank()) globalNickname = editNickname
                    globalAvatarIndex = editAvatar
                    showProfileEditDialog = false
                }) { Text("저장", color = AppPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showProfileEditDialog = false }) {
                    Text("취소", color = AppSubText)
                }
            }
        )
    }

    if (showLogoutDialog) {
        SettingsAlertDialog(
            title = "로그아웃",
            text = "정말 로그아웃 하시겠습니까?",
            confirmText = "로그아웃",
            confirmColor = Color(0xFFFF6B6B),
            onConfirm = {
                showLogoutDialog = false
                TokenManager.logout()
                isLoggedIn = false
                onLogout()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }

    if (showWithdrawDialog) {
        SettingsAlertDialog(
            title = "회원 탈퇴",
            text = "탈퇴 시 모든 데이터가 삭제되며 복구할 수 없습니다.\n정말 탈퇴하시겠습니까?",
            confirmText = "탈퇴",
            confirmColor = Color(0xFFFF6B6B),
            onConfirm = { showWithdrawDialog = false },
            onDismiss = { showWithdrawDialog = false }
        )
    }

    if (showQualityDialog) {
        val options = listOf("고음질", "일반", "저용량")
        SettingsSingleChoiceDialog(
            title = "스트리밍 음질",
            options = options,
            selected = streamingQuality,
            onSelect = { streamingQuality = it; showQualityDialog = false },
            onDismiss = { showQualityDialog = false }
        )
    }

    if (showDarkModeDialog) {
        val options = listOf("시스템 설정", "항상 다크 모드", "항상 라이트 모드")
        SettingsSingleChoiceDialog(
            title = "다크 모드",
            options = options,
            selected = darkMode,
            onSelect = { selected ->
                darkMode = selected
                appThemeMode = when (selected) {
                    "항상 다크 모드"  -> ThemeMode.DARK
                    "항상 라이트 모드" -> ThemeMode.LIGHT
                    else              -> ThemeMode.SYSTEM
                }
                showDarkModeDialog = false
            },
            onDismiss = { showDarkModeDialog = false }
        )
    }

    if (showEqDialog) {
        val eqOptions = listOf("기본", "베이스 강화", "팝", "클래식", "재즈", "일렉트로닉")
        SettingsSingleChoiceDialog(
            title = "이퀄라이저 (EQ)",
            options = eqOptions,
            selected = selectedEq,
            onSelect = { selectedEq = it; showEqDialog = false },
            onDismiss = { showEqDialog = false }
        )
    }

    if (showVersionDialog) {
        AlertDialog(
            onDismissRequest = { showVersionDialog = false },
            containerColor = AppSurface,
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(36.dp)) },
            title = { Text("버전 정보", color = AppText, fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("현재 버전 v1.0.0", color = AppSubText, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("최신 버전입니다 ✓", color = AppPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                TextButton(onClick = { showVersionDialog = false }) {
                    Text("확인", color = AppPrimary)
                }
            }
        )
    }

    if (showLicenseDialog) {
        AlertDialog(
            onDismissRequest = { showLicenseDialog = false },
            containerColor = AppSurface,
            title = { Text("오픈소스 라이선스", color = AppText, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LicenseItem("Jetpack Compose", "Apache License 2.0")
                    LicenseItem("Retrofit2", "Apache License 2.0")
                    LicenseItem("OkHttp3", "Apache License 2.0")
                    LicenseItem("Kotlin Coroutines", "Apache License 2.0")
                    LicenseItem("Material Icons", "Apache License 2.0")
                }
            },
            confirmButton = {
                TextButton(onClick = { showLicenseDialog = false }) {
                    Text("닫기", color = AppPrimary)
                }
            }
        )
    }

    // ── 메인 화면 ──────────────────────────────────

    Scaffold(
        containerColor = AppBg,
        topBar = {
            TopAppBar(
                title = { Text("환경설정", color = AppText, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기", tint = AppText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBg)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AppBg)
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // ── 계정 및 프로필 ──
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { SettingsSectionHeader("계정 및 프로필") }

            item {
                if (isLoggedIn) {
                    // 로그인 상태 - 프로필 카드
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AppSurface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AvatarCircle(index = globalAvatarIndex, size = 56)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    globalNickname,
                                    color = AppText, fontSize = 17.sp, fontWeight = FontWeight.Bold
                                )
                                Text("블라인드 크리에이터", color = AppSubText, fontSize = 13.sp)
                            }
                            TextButton(onClick = {
                                editNickname = globalNickname
                                editAvatar = globalAvatarIndex
                                showProfileEditDialog = true
                            }) {
                                Text("수정", color = AppPrimary, fontSize = 13.sp)
                            }
                        }
                    }
                } else {
                    // 비로그인 상태 - 로그인 유도 카드
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AppSurface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().clickable { onLoginRequired() }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(56.dp).clip(CircleShape).background(AppSurfaceHi),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = AppSubText, modifier = Modifier.size(32.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("로그인이 필요합니다", color = AppText, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                Text("탭하여 로그인하기", color = AppSubText, fontSize = 13.sp)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AppSubText)
                        }
                    }
                }
            }

            if (isLoggedIn) {
                item {
                    SettingsClickItem(
                        title = "로그아웃",
                        icon = Icons.Default.Logout,
                        titleColor = Color(0xFFFF6B6B),
                        onClick = { showLogoutDialog = true }
                    )
                }
                item {
                    SettingsClickItem(
                        title = "회원 탈퇴",
                        icon = Icons.Default.PersonRemove,
                        titleColor = Color(0xFFFF6B6B),
                        onClick = { showWithdrawDialog = true }
                    )
                }
            }

            // ── 재생 및 음질 ──
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { SettingsSectionHeader("재생 및 음질") }

            item {
                SettingsValueItem(
                    title = "스트리밍 음질",
                    icon = Icons.Default.HighQuality,
                    value = streamingQuality,
                    onClick = { showQualityDialog = true }
                )
            }
            item {
                SettingsValueItem(
                    title = "이퀄라이저 (EQ)",
                    icon = Icons.Default.Tune,
                    value = selectedEq,
                    onClick = { showEqDialog = true }
                )
            }

            // ── 앱 설정 ──
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { SettingsSectionHeader("앱 설정") }

            item {
                SettingsValueItem(
                    title = "다크 모드",
                    icon = Icons.Default.DarkMode,
                    value = darkMode,
                    onClick = { showDarkModeDialog = true }
                )
            }
            item {
                SettingsToggleItem(
                    title = "푸시 알림",
                    subtitle = "새로운 인기 차트 등 알림",
                    icon = Icons.Default.Notifications,
                    checked = pushNotification,
                    onCheckedChange = { pushNotification = it }
                )
            }

            // ── 정보 및 지원 ──
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { SettingsSectionHeader("정보 및 지원") }

            item {
                SettingsValueItem(
                    title = "앱 버전",
                    icon = Icons.Default.Info,
                    value = "v1.0.0",
                    onClick = { showVersionDialog = true }
                )
            }
            item {
                SettingsClickItem(
                    title = "오픈소스 라이선스",
                    icon = Icons.Default.Description,
                    onClick = { showLicenseDialog = true }
                )
            }
            item {
                SettingsClickItem(
                    title = "문의하기 (GitHub)",
                    icon = Icons.Default.Code,
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/jinwoo1125/team-khm"))
                        context.startActivity(intent)
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

// ── 공통 UI 컴포넌트들 ────────────────────────────

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        color = AppPrimary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
    )
}

@Composable
private fun SettingsStaticItem(
    title: String,
    icon: ImageVector,
    value: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Text(text = title, color = AppText, fontSize = 15.sp, modifier = Modifier.weight(1f))
            Text(text = value, color = AppSubText, fontSize = 13.sp)
        }
    }
}

@Composable
private fun SettingsClickItem(
    title: String,
    icon: ImageVector,
    titleColor: Color = AppText,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = titleColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Text(text = title, color = titleColor, fontSize = 15.sp, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AppSubText)
        }
    }
}

@Composable
private fun SettingsValueItem(
    title: String,
    icon: ImageVector,
    value: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Text(text = title, color = AppText, fontSize = 15.sp, modifier = Modifier.weight(1f))
            Text(text = value, color = AppSubText, fontSize = 13.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AppSubText)
        }
    }
}

@Composable
private fun SettingsToggleItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = AppPrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = AppText, fontSize = 15.sp)
                Text(text = subtitle, color = AppSubText, fontSize = 12.sp)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AppBg,
                    checkedTrackColor = AppPrimary,
                    uncheckedThumbColor = AppSubText,
                    uncheckedTrackColor = AppSurfaceHi
                )
            )
        }
    }
}

@Composable
private fun SettingsAlertDialog(
    title: String,
    text: String,
    confirmText: String,
    confirmColor: Color,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppSurface,
        title = { Text(title, color = AppText, fontWeight = FontWeight.Bold) },
        text = { Text(text, color = AppSubText) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(confirmText, color = confirmColor) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소", color = AppSubText) }
        }
    )
}

@Composable
private fun SettingsSingleChoiceDialog(
    title: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppSurface,
        title = { Text(title, color = AppText, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = option == selected,
                            onClick = { onSelect(option) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = AppPrimary,
                                unselectedColor = AppSubText
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(option, color = if (option == selected) AppPrimary else AppText, fontSize = 15.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("닫기", color = AppSubText) }
        }
    )
}

@Composable
private fun LicenseItem(name: String, license: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(name, color = AppText, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Text(license, color = AppSubText, fontSize = 12.sp)
    }
}

// ── 아바타 데이터 ──────────────────────────────────
private val avatarList = listOf(
    Pair("🐻", Color(0xFFD4956A)), // 곰
    Pair("🐰", Color(0xFFFFB7C5)), // 토끼
    Pair("🐱", Color(0xFFB0A0CC)), // 고양이
    Pair("🦊", Color(0xFFFF8C42)), // 여우
    Pair("🐨", Color(0xFF8BBFB8)), // 코알라
    Pair("🐘", Color(0xFFAABBCC)), // 코끼리
    Pair("🐥", Color(0xFFFFD166)), // 오리
    Pair("🦉", Color(0xFF9B7EC8)), // 부엉이
    Pair("🐕", Color(0xFFE8935A)), // 시바
    Pair("🐸", Color(0xFF72C472))  // 개구리
)

@Composable
fun AvatarCircle(index: Int, size: Int) {
    val avatar = avatarList.getOrElse(index) { avatarList[0] }
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(avatar.second),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = avatar.first,
            fontSize = (size * 0.5).sp
        )
    }
}
