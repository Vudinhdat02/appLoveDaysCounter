package com.example.lovedays.ui.components

import android.provider.Settings
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/** true nếu hệ thống không bật "Giảm chuyển động" / tắt animation. */
@Composable
fun rememberAnimationsEnabled(): Boolean {
    val context = LocalContext.current
    return remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
    }
}

/** Vẽ một trái tim có tâm tại [center], rộng khoảng [size]. */
fun DrawScope.drawHeart(center: Offset, size: Float, color: Color, rotationDegrees: Float = 0f) {
    val s = size / 2f
    val path = Path().apply {
        moveTo(center.x, center.y + s * 0.9f)
        cubicTo(center.x - s * 1.6f, center.y - s * 0.1f, center.x - s * 0.7f, center.y - s * 1.3f, center.x, center.y - s * 0.45f)
        cubicTo(center.x + s * 0.7f, center.y - s * 1.3f, center.x + s * 1.6f, center.y - s * 0.1f, center.x, center.y + s * 0.9f)
        close()
    }
    rotate(rotationDegrees, center) { drawPath(path, color) }
}

private class Particle(
    val x: Float,
    val size: Float,
    val speed: Float,
    val phase: Float,
    val sway: Float,
    val alpha: Float,
    val tint: Int,
)

/**
 * Các trái tim nhỏ bay lên (hoặc rơi xuống khi [falling] = true, dùng cho ngày kỷ niệm).
 * Không chạy animation khi hệ thống bật giảm chuyển động.
 */
@Composable
fun FloatingHearts(
    modifier: Modifier = Modifier,
    count: Int = 16,
    falling: Boolean = false,
    colors: List<Color> = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary,
        Color(0xFFFF8FA8),
    ),
    maxAlpha: Float = 0.35f,
    seed: Int = 7,
) {
    val enabled = rememberAnimationsEnabled()
    val particles = remember(count, seed) {
        val r = Random(seed)
        List(count) {
            Particle(
                x = r.nextFloat(),
                size = 10f + r.nextFloat() * 26f,
                speed = 0.6f + r.nextFloat() * 0.9f,
                phase = r.nextFloat(),
                sway = 8f + r.nextFloat() * 24f,
                alpha = 0.35f + r.nextFloat() * 0.65f,
                tint = r.nextInt(3),
            )
        }
    }
    val t = if (enabled) {
        val transition = rememberInfiniteTransition(label = "hearts")
        val v by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(if (falling) 6_000 else 18_000, easing = LinearEasing), RepeatMode.Restart),
            label = "heartsTime",
        )
        v
    } else {
        0.5f
    }
    Canvas(modifier.fillMaxSize()) {
        val density = this.density
        particles.forEach { p ->
            val progress = (p.phase + t * p.speed) % 1f
            val yFraction = if (falling) progress else 1f - progress
            val y = -40f + yFraction * (size.height + 80f)
            val x = p.x * size.width + sin((progress * 2 * PI * 1.5) + p.phase * 6).toFloat() * p.sway * density / 2
            // Mờ dần ở hai đầu màn hình.
            val edge = (minOf(progress, 1f - progress) * 5f).coerceIn(0f, 1f)
            drawHeart(
                center = Offset(x, y),
                size = p.size * density,
                color = colors[p.tint % colors.size].copy(alpha = maxAlpha * p.alpha * edge),
                rotationDegrees = sin(progress * 2 * PI + p.phase * 3).toFloat() * 18f,
            )
        }
    }
}

/** Nền gradient mềm với các trái tim bay nhẹ phía sau nội dung. */
@Composable
fun LoveBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val brush = remember(scheme) {
        Brush.verticalGradient(
            0f to scheme.primaryContainer.copy(alpha = 0.95f),
            0.45f to scheme.background,
            1f to scheme.tertiary.copy(alpha = 0.18f).compositeOver(scheme.background),
        )
    }
    Box(modifier.fillMaxSize().background(brush)) {
        FloatingHearts(maxAlpha = 0.22f)
        content()
    }
}

private fun Color.compositeOver(background: Color): Color {
    val a = alpha
    return Color(
        red = red * a + background.red * (1 - a),
        green = green * a + background.green * (1 - a),
        blue = blue * a + background.blue * (1 - a),
        alpha = 1f,
    )
}

