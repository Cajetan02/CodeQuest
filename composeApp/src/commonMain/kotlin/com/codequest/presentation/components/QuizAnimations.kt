package com.codequest.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random

// =====================================================================
//  CONFETTI BURST — celebratory particle system on correct answer
// =====================================================================

private data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val velocityX: Float,
    val velocityY: Float,
    val rotation: Float,
    val rotationSpeed: Float,
    val color: Color,
    val size: Float,
    val shape: Int // 0 = circle, 1 = rect, 2 = triangle
)

@Composable
fun ConfettiBurst(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isActive) return

    val particles = remember {
        val colors = listOf(
            Color(0xFF6200EE), Color(0xFF03DAC6), Color(0xFFFF6D00),
            Color(0xFFFFD600), Color(0xFFFF1744), Color(0xFF00E676),
            Color(0xFF2979FF), Color(0xFFE040FB)
        )
        List(60) {
            ConfettiParticle(
                x = 0.5f + (Random.nextFloat() - 0.5f) * 0.3f,
                y = 0.4f,
                velocityX = (Random.nextFloat() - 0.5f) * 12f,
                velocityY = -(Random.nextFloat() * 8f + 4f),
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 15f,
                color = colors.random(),
                size = 4f + Random.nextFloat() * 8f,
                shape = Random.nextInt(3)
            )
        }
    }

    val time by rememberInfiniteTransition(label = "confetti_time").animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confetti_t"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isActive) 1f else 0f,
        animationSpec = tween(200),
        label = "confetti_alpha"
    )

    Canvas(modifier = modifier.fillMaxSize().graphicsLayer { this.alpha = alpha }) {
        val w = size.width
        val h = size.height
        val gravity = 6f

        particles.forEach { p ->
            val t = time
            val px = (p.x * w + p.velocityX * t * 40f)
            val py = (p.y * h + p.velocityY * t * 40f + gravity * t * t * 60f)
            val particleAlpha = (1f - t / 3f).coerceIn(0f, 1f)

            if (py < h && particleAlpha > 0.05f) {
                val rot = p.rotation + p.rotationSpeed * t * 60f

                when (p.shape) {
                    0 -> drawCircle(
                        color = p.color.copy(alpha = particleAlpha),
                        radius = p.size,
                        center = Offset(px, py)
                    )
                    1 -> drawRect(
                        color = p.color.copy(alpha = particleAlpha),
                        topLeft = Offset(px - p.size / 2, py - p.size / 2),
                        size = androidx.compose.ui.geometry.Size(p.size, p.size * 0.6f)
                    )
                    2 -> drawCircle(
                        color = p.color.copy(alpha = particleAlpha * 0.7f),
                        radius = p.size * 0.7f,
                        center = Offset(px, py)
                    )
                }
            }
        }
    }
}

// =====================================================================
//  XP POPUP — "+10 XP" floating up animation
// =====================================================================

@Composable
fun XpPopup(
    xpAmount: Int,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    var show by remember(isVisible) { mutableStateOf(isVisible) }

    val offsetY by animateFloatAsState(
        targetValue = if (show) -80f else 0f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "xp_offset"
    )

    val alpha by animateFloatAsState(
        targetValue = if (show) 0f else 1f,
        animationSpec = tween(800, delayMillis = 400),
        label = "xp_alpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (show) 1.3f else 0.8f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
        label = "xp_scale"
    )

    if (isVisible) {
        Box(
            modifier = modifier
                .graphicsLayer {
                    translationY = offsetY
                    this.alpha = 1f - alpha
                    scaleX = scale
                    scaleY = scale
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+$xpAmount XP",
                color = Color(0xFF03DAC6),
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

// =====================================================================
//  SHAKE ANIMATION — for incorrect answers
// =====================================================================

@Composable
fun rememberShakeAnimation(trigger: Boolean): Float {
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(trigger) {
        if (trigger) {
            // Rapid shake back and forth
            repeat(4) {
                shakeOffset.animateTo(12f, tween(40))
                shakeOffset.animateTo(-12f, tween(40))
            }
            shakeOffset.animateTo(0f, tween(40))
        }
    }

    return shakeOffset.value
}

// =====================================================================
//  STREAK FLAME — fire animation for consecutive correct answers
// =====================================================================

@Composable
fun StreakFlame(
    streak: Int,
    modifier: Modifier = Modifier
) {
    if (streak < 2) return

    val infiniteTransition = rememberInfiniteTransition(label = "flame")
    val flicker by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_flicker"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_pulse"
    )

    Row(
        modifier = modifier.graphicsLayer {
            scaleX = pulseScale
            scaleY = pulseScale
            alpha = flicker
        },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🔥",
            fontSize = (16 + streak.coerceAtMost(5) * 4).sp
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "${streak}x Streak!",
            color = Color(0xFFFF6D00),
            fontSize = 14.sp,
            fontWeight = FontWeight.Black
        )
    }
}

// =====================================================================
//  HEARTS / LIVES DISPLAY
// =====================================================================

@Composable
fun HeartsDisplay(
    currentHearts: Int,
    maxHearts: Int = 3,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(maxHearts) { index ->
            val isAlive = index < currentHearts
            val heartScale by animateFloatAsState(
                targetValue = if (isAlive) 1f else 0.6f,
                animationSpec = spring(dampingRatio = 0.4f, stiffness = 400f),
                label = "heart_$index"
            )
            Text(
                text = if (isAlive) "❤️" else "🩶",
                fontSize = 20.sp,
                modifier = Modifier.graphicsLayer {
                    scaleX = heartScale
                    scaleY = heartScale
                }
            )
        }
    }
}

// =====================================================================
//  ANIMATED STAR RATING — for lesson complete
// =====================================================================

@Composable
fun AnimatedStarRating(
    starCount: Int, // 1-3
    modifier: Modifier = Modifier
) {
    var animateStars by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(400)
        animateStars = true
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val earned = index < starCount
            val shouldAnimate = animateStars && earned

            val scale by animateFloatAsState(
                targetValue = if (shouldAnimate) 1f else 0f,
                animationSpec = spring(
                    dampingRatio = 0.4f,
                    stiffness = 200f
                ),
                label = "star_$index"
            )

            val rotation by animateFloatAsState(
                targetValue = if (shouldAnimate) 0f else -180f,
                animationSpec = tween(
                    durationMillis = 600,
                    delayMillis = index * 300,
                    easing = FastOutSlowInEasing
                ),
                label = "star_rot_$index"
            )

            Text(
                text = if (earned) "⭐" else "☆",
                fontSize = 48.sp,
                modifier = Modifier.graphicsLayer {
                    scaleX = if (earned) scale else 0.5f
                    scaleY = if (earned) scale else 0.5f
                    rotationZ = if (earned) rotation else 0f
                    alpha = if (earned) scale else 0.3f
                }
            )
        }
    }
}

// =====================================================================
//  ANIMATED XP COUNTER — for lesson complete
// =====================================================================

@Composable
fun AnimatedXpCounter(
    targetXp: Int,
    modifier: Modifier = Modifier
) {
    var currentXp by remember { mutableIntStateOf(0) }

    LaunchedEffect(targetXp) {
        if (targetXp > 0) {
            delay(800) // wait for stars
            val steps = 30
            val increment = targetXp / steps
            repeat(steps) {
                currentXp = ((it + 1) * targetXp) / steps
                delay(30)
            }
            currentXp = targetXp
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (currentXp > 0) 1f else 0.5f,
        animationSpec = spring(dampingRatio = 0.5f),
        label = "xp_counter_scale"
    )

    Text(
        text = "+$currentXp XP",
        fontSize = 42.sp,
        fontWeight = FontWeight.Black,
        color = Color(0xFF03DAC6),
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    )
}

// =====================================================================
//  GLOWING PROGRESS BAR — with animated glow effect
// =====================================================================

@Composable
fun GlowingProgressBar(
    progress: Float,
    checkpointCount: Int,
    currentCheckpoint: Int,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "progress_bar"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(modifier = modifier.height(14.dp).fillMaxWidth()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cornerRadius = h / 2f

            // Background track
            drawRoundRect(
                color = Color(0xFF2D2D3D),
                size = size,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius)
            )

            // Filled progress
            val filledWidth = w * animatedProgress
            if (filledWidth > 0) {
                drawRoundRect(
                    color = Color(0xFF6200EE),
                    size = size.copy(width = filledWidth),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius)
                )

                // Glow at leading edge
                drawCircle(
                    color = Color(0xFF6200EE).copy(alpha = glowAlpha),
                    radius = h * 1.2f,
                    center = Offset(filledWidth, h / 2f)
                )
            }

            // Checkpoint dots
            if (checkpointCount > 1) {
                for (i in 1 until checkpointCount) {
                    val cx = w * (i.toFloat() / checkpointCount)
                    val passed = i <= currentCheckpoint
                    drawCircle(
                        color = if (passed) Color(0xFF03DAC6) else Color(0xFF4A4A5A),
                        radius = h * 0.3f,
                        center = Offset(cx, h / 2f)
                    )
                }
            }
        }
    }
}
