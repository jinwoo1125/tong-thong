package com.example.noisecanseling

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

// ── 전역 로그인 상태 ──────────────────────────────
var isLoggedIn by mutableStateOf(false)

// ── 전역 프로필 상태 ──────────────────────────────
var globalAvatarIndex by mutableStateOf(0)
var globalNickname by mutableStateOf("유저 이름")

// ── 테마 모드 ─────────────────────────────────────
enum class ThemeMode { SYSTEM, DARK, LIGHT }

// 전역 테마 상태 (앱 전체에서 공유)
var appThemeMode by mutableStateOf(ThemeMode.DARK)

// ── 색상 묶음 ─────────────────────────────────────
data class AppColorScheme(
    val bg: Color,
    val surface: Color,
    val surfaceHi: Color,
    val primary: Color,
    val accent: Color,
    val green: Color,
    val text: Color,
    val subText: Color
)

private val DarkColors = AppColorScheme(
    bg        = Color(0xFF0E1117),
    surface   = Color(0xFF171B24),
    surfaceHi = Color(0xFF202736),
    primary   = Color(0xFFFFC857),
    accent    = Color(0xFF58C4DD),
    green     = Color(0xFF35D07F),
    text      = Color.White,
    subText   = Color(0xFF9BA3B4)
)

private val LightColors = AppColorScheme(
    bg        = Color(0xFFF5F6FA),
    surface   = Color(0xFFFFFFFF),
    surfaceHi = Color(0xFFE8EAF0),
    primary   = Color(0xFFD4960A),
    accent    = Color(0xFF0099BB),
    green     = Color(0xFF1A9E5A),
    text      = Color(0xFF1A1D26),
    subText   = Color(0xFF6B7280)
)

val LocalAppColors = compositionLocalOf { DarkColors }

@Composable
fun AppTheme(
    isSystemDark: Boolean,
    content: @Composable () -> Unit
) {
    val colors = when (appThemeMode) {
        ThemeMode.DARK   -> DarkColors
        ThemeMode.LIGHT  -> LightColors
        ThemeMode.SYSTEM -> if (isSystemDark) DarkColors else LightColors
    }
    CompositionLocalProvider(LocalAppColors provides colors) {
        content()
    }
}

// ── 색상 접근 프로퍼티 (기존 코드와 호환) ─────────
val AppBg        @Composable get() = LocalAppColors.current.bg
val AppSurface   @Composable get() = LocalAppColors.current.surface
val AppSurfaceHi @Composable get() = LocalAppColors.current.surfaceHi
val AppPrimary   @Composable get() = LocalAppColors.current.primary
val AppAccent    @Composable get() = LocalAppColors.current.accent
val AppGreen     @Composable get() = LocalAppColors.current.green
val AppText      @Composable get() = LocalAppColors.current.text
val AppSubText   @Composable get() = LocalAppColors.current.subText
