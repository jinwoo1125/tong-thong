package com.example.noisecanseling

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noisecanseling.network.LoginRequest
import com.example.noisecanseling.network.RegisterRequest
import com.example.noisecanseling.network.RetrofitClient
import com.example.noisecanseling.network.TokenManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val BackgroundColor = Color(0xFF0E1117)
private val SurfaceColor = Color(0xFF171B24)
private val PrimaryColor = Color(0xFFFFC857)
private val AccentColor = Color(0xFF58C4DD)
private val TextColor = Color.White
private val SubTextColor = Color(0xFF9BA3B4)
private val ErrorColor = Color(0xFFFF6B6B)

private val AuthTextFieldColors
    @Composable get() = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = PrimaryColor,
        unfocusedBorderColor = SubTextColor,
        focusedLabelColor = PrimaryColor,
        unfocusedLabelColor = SubTextColor,
        cursorColor = PrimaryColor,
        focusedTextColor = TextColor,
        unfocusedTextColor = TextColor
    )

@Composable
fun LoginScreen(
    onSignUpClick: () -> Unit = {},
    onFindClick: () -> Unit = {},
    onLoginSuccess: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize().background(BackgroundColor)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(96.dp))

            Text(
                text = "noise canceling",
                fontSize = 30.sp,
                fontWeight = FontWeight.Light,
                fontStyle = FontStyle.Italic,
                letterSpacing = 2.sp,
                color = PrimaryColor
            )

            Spacer(modifier = Modifier.height(64.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("이메일") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                colors = AuthTextFieldColors
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("비밀번호") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                colors = AuthTextFieldColors
            )

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(errorMessage, color = ErrorColor, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onFindClick) {
                    Text(text = "아이디 및 비밀번호 찾기", fontSize = 12.sp, color = SubTextColor)
                }
                Text(text = "|", fontSize = 12.sp, color = SubTextColor)
                TextButton(onClick = onSignUpClick) {
                    Text(text = "회원가입", fontSize = 12.sp, color = AccentColor)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "이메일과 비밀번호를 입력해주세요."
                        return@Button
                    }
                    // 마스터키 계정 (서버 없이 테스트용)
                    if (email == "admin" && password == "1234") {
                        TokenManager.saveToken("master_token", "NoiseAdmin")
                        isLoggedIn = true
                        globalNickname = "NoiseAdmin"
                        onLoginSuccess()
                        return@Button
                    }

                    scope.launch {
                        isLoading = true
                        errorMessage = ""
                        try {
                            val response = RetrofitClient.api.login(LoginRequest(email, password))
                            if (response.isSuccessful) {
                                val body = response.body()!!
                                TokenManager.saveToken(body.token, body.nickname)
                                isLoggedIn = true
                                globalNickname = body.nickname
                                onLoginSuccess()
                            } else {
                                errorMessage = when (response.code()) {
                                    401 -> "이메일 또는 비밀번호가 올바르지 않습니다."
                                    else -> "로그인에 실패했습니다."
                                }
                            }
                        } catch (e: Exception) {
                            errorMessage = "서버에 연결할 수 없습니다."
                        }
                        isLoading = false
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor, disabledContainerColor = PrimaryColor.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = BackgroundColor)
                } else {
                    Text(text = "LOGIN", letterSpacing = 3.sp, fontWeight = FontWeight.Bold, color = BackgroundColor)
                }
            }
        }

        // 테스트 계정 안내
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .background(
                    color = SurfaceColor,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
                )
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            androidx.compose.foundation.layout.Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("테스트 계정", color = SubTextColor, fontSize = 11.sp)
                Text("ID: admin  /  PW: 1234", color = PrimaryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // 왼쪽 위 뒤로가기 버튼 (Box 스코프에서 align 사용)
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.padding(8.dp).align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "뒤로가기",
                tint = PrimaryColor
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onBackClick: () -> Unit = {},
    onSignUpComplete: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    var timerSeconds by remember { mutableIntStateOf(180) }
    var timerRunning by remember { mutableStateOf(false) }

    LaunchedEffect(timerRunning) {
        if (timerRunning) {
            while (timerSeconds > 0) {
                delay(1000L)
                timerSeconds--
            }
            timerRunning = false
        }
    }

    val timerText = remember(timerSeconds) {
        val m = timerSeconds / 60
        val s = timerSeconds % 60
        "%d:%02d".format(m, s)
    }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("회원가입", color = TextColor) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기", tint = TextColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundColor)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor)
                .padding(innerPadding)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("이메일") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.weight(1f),
                    colors = AuthTextFieldColors
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { timerSeconds = 180; timerRunning = true },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(56.dp)
                ) {
                    Text("인증", fontSize = 13.sp, color = BackgroundColor)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = timerText,
                    fontSize = 14.sp,
                    color = if (timerRunning) ErrorColor else SubTextColor,
                    modifier = Modifier.widthIn(min = 36.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = verificationCode,
                onValueChange = { verificationCode = it },
                label = { Text("인증번호") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = AuthTextFieldColors
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("비밀번호") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                colors = AuthTextFieldColors
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = passwordConfirm,
                onValueChange = { passwordConfirm = it },
                label = { Text("비밀번호 확인") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                colors = AuthTextFieldColors
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "닉네임: 랜덤 (가입 시 자동 생성)", fontSize = 12.sp, color = SubTextColor, modifier = Modifier.fillMaxWidth())

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(errorMessage, color = ErrorColor, fontSize = 13.sp, modifier = Modifier.fillMaxWidth())
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "이메일과 비밀번호를 입력해주세요."; return@Button
                    }
                    if (password != passwordConfirm) {
                        errorMessage = "비밀번호가 일치하지 않습니다."; return@Button
                    }
                    scope.launch {
                        isLoading = true
                        errorMessage = ""
                        try {
                            val response = RetrofitClient.api.register(RegisterRequest(email, password))
                            if (response.isSuccessful) {
                                val body = response.body()!!
                                TokenManager.saveToken(body.token, body.nickname)
                                isLoggedIn = true
                                onSignUpComplete()
                            } else {
                                errorMessage = when (response.code()) {
                                    409 -> "이미 사용 중인 이메일입니다."
                                    else -> "회원가입에 실패했습니다."
                                }
                            }
                        } catch (e: Exception) {
                            errorMessage = "서버에 연결할 수 없습니다."
                        }
                        isLoading = false
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor, disabledContainerColor = PrimaryColor.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = BackgroundColor)
                } else {
                    Text("가입하기", fontWeight = FontWeight.Bold, color = BackgroundColor)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun CompleteScreen(
    onGoToLoginClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(96.dp))

        Text(
            text = "noise canceling",
            fontSize = 30.sp,
            fontWeight = FontWeight.Light,
            fontStyle = FontStyle.Italic,
            letterSpacing = 2.sp,
            color = PrimaryColor
        )

        Spacer(modifier = Modifier.weight(1f))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "✦", fontSize = 28.sp, color = PrimaryColor)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "가입이 완료되었습니다", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, color = TextColor)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "noise canceling과 함께해 주셔서 감사합니다", fontSize = 13.sp, color = SubTextColor, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "✦", fontSize = 28.sp, color = PrimaryColor)
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onGoToLoginClick,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("로그인하러 가기", fontWeight = FontWeight.Bold, color = BackgroundColor)
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindPasswordScreen(
    onBackClick: () -> Unit = {},
    onLoginClick: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var isVerified by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }
    var newPasswordConfirm by remember { mutableStateOf("") }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("아이디 및 비밀번호 찾기", color = TextColor) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기", tint = TextColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundColor)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor)
                .padding(innerPadding)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("이메일") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.weight(1f),
                    colors = AuthTextFieldColors
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(56.dp)
                ) {
                    Text("인증", fontSize = 13.sp, color = BackgroundColor)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = verificationCode,
                onValueChange = { verificationCode = it },
                label = { Text("인증코드") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = AuthTextFieldColors
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (!isVerified) {
                Button(
                    onClick = { isVerified = true },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("인증 확인", fontWeight = FontWeight.Bold, color = BackgroundColor)
                }
            } else {
                OutlinedButton(
                    onClick = { },
                    enabled = false,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text(text = "인증 완료", color = SubTextColor)
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(text = "비밀번호 재설정", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth(), color = TextColor)

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("새 비밀번호") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    colors = AuthTextFieldColors
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = newPasswordConfirm,
                    onValueChange = { newPasswordConfirm = it },
                    label = { Text("새 비밀번호 확인") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    colors = AuthTextFieldColors
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onLoginClick,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Login (비밀번호 변경)", fontWeight = FontWeight.Bold, color = BackgroundColor)
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Preview(showBackground = true, name = "1. 로그인 화면")
@Composable
fun LoginScreenPreview() {
    MaterialTheme { LoginScreen() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "2. 회원가입 화면")
@Composable
fun SignUpScreenPreview() {
    MaterialTheme { SignUpScreen() }
}

@Preview(showBackground = true, name = "3. 가입 완료 화면")
@Composable
fun CompleteScreenPreview() {
    MaterialTheme { CompleteScreen() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "4. 비밀번호 찾기 - 인증 전")
@Composable
fun FindPasswordScreenPreview() {
    MaterialTheme { FindPasswordScreen() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "5. 비밀번호 찾기 - 인증 후")
@Composable
fun FindPasswordScreenVerifiedPreview() {
    MaterialTheme { FindPasswordScreenVerifiedState() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindPasswordScreenVerifiedState() {
    var newPassword by remember { mutableStateOf("") }
    var newPasswordConfirm by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("아이디 및 비밀번호 찾기") },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = "test@email.com", onValueChange = {}, label = { Text("이메일") }, singleLine = true, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {}, modifier = Modifier.height(56.dp)) { Text("인증", fontSize = 13.sp) }
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = "123456", onValueChange = {}, label = { Text("인증코드") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                Text("인증 완료", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            }
            Spacer(modifier = Modifier.height(28.dp))
            Text("비밀번호 재설정", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = newPassword, onValueChange = { newPassword = it }, label = { Text("새 비밀번호") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = newPasswordConfirm, onValueChange = { newPasswordConfirm = it }, label = { Text("새 비밀번호 확인") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = {}, modifier = Modifier.fillMaxWidth().height(50.dp)) { Text("Login (비밀번호 변경)", fontWeight = FontWeight.Bold) }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
