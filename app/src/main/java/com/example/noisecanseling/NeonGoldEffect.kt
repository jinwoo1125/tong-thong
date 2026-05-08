package com.example.noisecanseling

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text

private val Gold   = Color(0xFFFFDA79)
private val Orange = Color(0xFFFF9F43)

// ── 네온 샴페인 골드 텍스트 ─────────────────────────────────────────────
// flickerEnabled=true 시 불규칙한 빛 깜빡임 애니메이션 동반
@Composable
fun NeonGoldText(
    text: String,
    fontSize: TextUnit = 30.sp,
    modifier: Modifier = Modifier,
    flickerEnabled: Boolean = false
) {
    val transition = rememberInfiniteTransition(label = "neonGold")

    val flickerAlpha by transition.animateFloat(
        initialValue = if (flickerEnabled) 0.78f else 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (flickerEnabled) 115 else 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flickerAlpha"
    )

    val glowIntensity by transition.animateFloat(
        initialValue = if (flickerEnabled) 0.55f else 0.85f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (flickerEnabled) 820 else 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowIntensity"
    )

    Text(
        text = text,
        color = Gold.copy(alpha = flickerAlpha),
        textAlign = TextAlign.Center,
        style = TextStyle(
            fontFamily = BebasNeueFontFamily,
            fontWeight = FontWeight.Black,
            fontSize = fontSize,
            letterSpacing = 3.sp
        ),
        modifier = modifier.drawBehind {
            val gi = glowIntensity
            drawIntoCanvas { canvas ->
                // 레이어 1 — 넓은 외곽 글로우 (오렌지)
                val paintOuter = Paint().also { p ->
                    p.asFrameworkPaint().apply {
                        isAntiAlias = true
                        color = android.graphics.Color.TRANSPARENT
                        setShadowLayer(64f, 0f, 0f, Orange.copy(alpha = 0.30f * gi).toArgb())
                    }
                }
                // 레이어 2 — 중간 글로우 (골드)
                val paintMid = Paint().also { p ->
                    p.asFrameworkPaint().apply {
                        isAntiAlias = true
                        color = android.graphics.Color.TRANSPARENT
                        setShadowLayer(28f, 0f, 0f, Gold.copy(alpha = 0.60f * gi).toArgb())
                    }
                }
                // 레이어 3 — 코어 글로우 (화이트 골드)
                val paintCore = Paint().also { p ->
                    p.asFrameworkPaint().apply {
                        isAntiAlias = true
                        color = android.graphics.Color.TRANSPARENT
                        setShadowLayer(10f, 0f, 0f, Gold.copy(alpha = 0.90f * gi).toArgb())
                    }
                }
                // 각 레이어를 center rect에 그려 그림자만 노출
                val cx = size.width / 2f
                val cy = size.height / 2f
                canvas.drawCircle(androidx.compose.ui.geometry.Offset(cx, cy), 1f, paintOuter)
                canvas.drawCircle(androidx.compose.ui.geometry.Offset(cx, cy), 1f, paintMid)
                canvas.drawCircle(androidx.compose.ui.geometry.Offset(cx, cy), 1f, paintCore)
            }
        }
    )
}

// ── 네온 골드 테두리 Modifier ─────────────────────────────────────────
// 각진 버튼·카드의 테두리에 글로우 효과를 줍니다.
fun Modifier.neonGoldBorder(
    width: Dp = 1.5.dp,
    glowRadius: Float = 18f,
    cornerRadius: Dp = 0.dp,
    intensity: Float = 1.0f
): Modifier = this
    .drawBehind {
        drawIntoCanvas { canvas ->
            val strokePx = width.toPx()
            val cr = cornerRadius.toPx()
            val inset = strokePx / 2f

            fun makePath() = Path().apply {
                addRoundRect(
                    RoundRect(
                        left = inset,
                        top = inset,
                        right = size.width - inset,
                        bottom = size.height - inset,
                        cornerRadius = CornerRadius(cr)
                    )
                )
            }

            fun glowPaint(blurR: Float, alpha: Float, color: Color) = Paint().also { p ->
                p.asFrameworkPaint().apply {
                    isAntiAlias = true
                    style = android.graphics.Paint.Style.STROKE
                    strokeWidth = strokePx
                    this.color = android.graphics.Color.TRANSPARENT
                    setShadowLayer(blurR, 0f, 0f, color.copy(alpha = alpha * intensity).toArgb())
                }
            }

            // 3단계 글로우
            canvas.drawPath(makePath(), glowPaint(glowRadius * 1.8f, 0.28f, Orange))
            canvas.drawPath(makePath(), glowPaint(glowRadius,         0.55f, Gold))
            canvas.drawPath(makePath(), glowPaint(glowRadius * 0.35f, 0.92f, Gold))
        }
    }
    .border(width, Gold.copy(alpha = 0.85f), RoundedCornerShape(cornerRadius))

// ── 선택 상태 조건부 적용 ──────────────────────────────────────────────
fun Modifier.neonGoldSelected(
    selected: Boolean,
    width: Dp = 1.5.dp,
    cornerRadius: Dp = 0.dp
): Modifier = if (selected) neonGoldBorder(width = width, cornerRadius = cornerRadius) else this

// ── 펄스 애니메이션 버전 (항상 켜져 있는 버튼 강조용) ──────────────────
@Composable
fun Modifier.animatedNeonGoldBorder(
    width: Dp = 1.5.dp,
    cornerRadius: Dp = 0.dp
): Modifier {
    val transition = rememberInfiniteTransition(label = "neonBorderPulse")
    val intensity by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "borderIntensity"
    )
    return neonGoldBorder(width = width, cornerRadius = cornerRadius, intensity = intensity)
}
