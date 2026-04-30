package com.codequest.presentation.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codequest.presentation.components.AnimatedStarRating
import com.codequest.presentation.components.AnimatedXpCounter
import com.codequest.presentation.components.ConfettiBurst
import kotlinx.coroutines.delay

@Composable
fun LessonCompleteScreen(
    score: Int,
    maxScore: Int,
    onContinue: () -> Unit
) {
    val percentage = if (maxScore > 0) score.toFloat() / maxScore else 0f
    val starCount = when {
        percentage >= 0.9f -> 3
        percentage >= 0.6f -> 2
        percentage > 0f -> 1
        else -> 0
    }

    var visible by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
        delay(600)
        showConfetti = true
    }

    // Animated gradient background
    val infiniteTransition = rememberInfiniteTransition(label = "bg_gradient")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient_shift"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Background gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0A0A12),
                            Color(0xFF0D1117),
                            Color(0xFF6200EE).copy(alpha = 0.06f + gradientOffset * 0.04f),
                            Color(0xFF0A0A12)
                        )
                    )
                )
        )

        // Confetti
        ConfettiBurst(isActive = showConfetti)

        // Main content
        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn(),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Trophy / completion title
                Text(
                    text = when {
                        percentage >= 0.9f -> "🏆"
                        percentage >= 0.6f -> "🎉"
                        percentage > 0f -> "👍"
                        else -> "📚"
                    },
                    fontSize = 72.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = when {
                        percentage >= 0.9f -> "Outstanding!"
                        percentage >= 0.6f -> "Great Job!"
                        percentage > 0f -> "Keep Learning!"
                        else -> "Try Again"
                    },
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Lesson Complete",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Star rating
                AnimatedStarRating(starCount = starCount)

                Spacer(modifier = Modifier.height(32.dp))

                // Score card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1A1A2E)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(28.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Total XP Earned",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Animated XP counter
                        AnimatedXpCounter(targetXp = score)

                        Spacer(modifier = Modifier.height(16.dp))

                        // Score breakdown
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ScoreStat(
                                label = "Accuracy",
                                value = "${(percentage * 100).toInt()}%",
                                color = when {
                                    percentage >= 0.8f -> Color(0xFF03DAC6)
                                    percentage >= 0.5f -> Color(0xFFFFD600)
                                    else -> Color(0xFFCF6679)
                                }
                            )
                            ScoreStat(
                                label = "Score",
                                value = "$score/$maxScore",
                                color = Color(0xFF6200EE)
                            )
                        }

                        if (score == maxScore && maxScore > 0) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Box(
                                modifier = Modifier
                                    .background(
                                        Color(0xFFFFD600).copy(alpha = 0.15f),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    "⭐ Perfect Score!",
                                    color = Color(0xFFFFD600),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Continue button
                Button(
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6200EE)
                    )
                ) {
                    Text(
                        "Continue",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ScoreStat(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = color,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black
        )
    }
}
