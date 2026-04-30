package com.codequest.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

// =====================================================================
//  1. WORD BANK — Build a sentence by tapping word chips (Duolingo style)
//     Uses: options = list of words (shuffled)
//           correctText = the correct sentence
// =====================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordBankLayout(
    options: List<String>,
    selectedAnswer: String?,
    isSubmitted: Boolean,
    isCorrect: Boolean,
    onAnswerChanged: (String) -> Unit
) {
    val selectedWords = selectedAnswer?.split(" ")?.filter { it.isNotBlank() } ?: emptyList()
    val availableWords = remember(options, selectedWords) {
        val sel = selectedWords.toMutableList()
        options.filter {
            val idx = sel.indexOf(it)
            if (idx >= 0) {
                sel.removeAt(idx)
                false
            } else true
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Answer area — where selected words appear
        Card(
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 72.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    isSubmitted && isCorrect -> Color(0xFF03DAC6).copy(alpha = 0.15f)
                    isSubmitted && !isCorrect -> Color(0xFFCF6679).copy(alpha = 0.15f)
                    else -> Color(0xFF1A1A1A).copy(alpha = 0.6f)
                }
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (selectedWords.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Tap words below to build your answer",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 14.sp
                    )
                }
            } else {
                // Word flow layout
                FlowRow(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectedWords.forEachIndexed { index, word ->
                        WordChip(
                            word = word,
                            isSelected = true,
                            isSubmitted = isSubmitted,
                            isCorrect = isCorrect,
                            onClick = {
                                if (!isSubmitted) {
                                    val newWords = selectedWords.toMutableList()
                                    newWords.removeAt(index)
                                    onAnswerChanged(newWords.joinToString(" "))
                                }
                            }
                        )
                    }
                }
            }
        }

        // Dashed line separator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(Color(0xFF6200EE).copy(alpha = 0.3f))
        )

        // Word bank — available words to tap
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            availableWords.forEach { word ->
                WordChip(
                    word = word,
                    isSelected = false,
                    isSubmitted = isSubmitted,
                    isCorrect = false,
                    onClick = {
                        if (!isSubmitted) {
                            val newAnswer = if (selectedWords.isEmpty()) word
                            else selectedWords.joinToString(" ") + " " + word
                            onAnswerChanged(newAnswer)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun WordChip(
    word: String,
    isSelected: Boolean,
    isSubmitted: Boolean,
    isCorrect: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = when {
            isSubmitted && isSelected && isCorrect -> Color(0xFF03DAC6).copy(alpha = 0.3f)
            isSubmitted && isSelected && !isCorrect -> Color(0xFFCF6679).copy(alpha = 0.3f)
            isSelected -> Color(0xFF6200EE).copy(alpha = 0.4f)
            else -> Color(0xFF2D2D3D)
        },
        animationSpec = tween(200),
        label = "chip_bg"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "chip_scale"
    )

    Box(
        modifier = Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(
                1.dp,
                if (isSelected) Color(0xFF6200EE).copy(alpha = 0.6f)
                else Color(0xFF4A4A5A),
                RoundedCornerShape(12.dp)
            )
            .clickable(enabled = !isSubmitted, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = word,
            color = Color.White.copy(alpha = if (isSubmitted && !isSelected) 0.4f else 0.9f),
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

// =====================================================================
//  2. LISTEN & TYPE — Audio prompt (visual speaker icon) + text input
//     Uses: correctText = the correct answer
//           prompt = what the "audio" says (shown after submit)
// =====================================================================

@Composable
fun ListenTypeLayout(
    prompt: String,
    selectedAnswer: String?,
    isSubmitted: Boolean,
    isCorrect: Boolean,
    correctText: String?,
    onAnswerChanged: (String) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "audio_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    var hasPlayed by remember { mutableStateOf(false) }
    val waveAlpha by animateFloatAsState(
        targetValue = if (hasPlayed) 0f else 1f,
        animationSpec = tween(2000),
        label = "wave"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Audio speaker button
        Card(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF6200EE).copy(alpha = 0.2f)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { hasPlayed = true }
                    .graphicsLayer {
                        scaleX = if (!hasPlayed) pulseScale else 1f
                        scaleY = if (!hasPlayed) pulseScale else 1f
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.VolumeUp,
                    contentDescription = "Listen",
                    tint = Color(0xFF6200EE),
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        // Audio wave visualization
        Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.graphicsLayer { alpha = waveAlpha }
        ) {
            val waveHeights = remember { List(12) { Random.nextFloat() * 0.7f + 0.3f } }
            waveHeights.forEach { heightFraction ->
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height((heightFraction * 32).dp)
                        .background(
                            Color(0xFF6200EE).copy(alpha = 0.6f),
                            RoundedCornerShape(2.dp)
                        )
                )
            }
        }

        Text(
            text = "Type what you hear",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp
        )

        // Input field
        OutlinedTextField(
            value = selectedAnswer ?: "",
            onValueChange = onAnswerChanged,
            enabled = !isSubmitted,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Type your answer...") },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF6200EE),
                unfocusedBorderColor = Color(0xFF4A4A5A),
                disabledBorderColor = if (isSubmitted) {
                    if (isCorrect) Color(0xFF03DAC6) else Color(0xFFCF6679)
                } else Color(0xFF4A4A5A),
                focusedContainerColor = Color(0xFF1A1A1A).copy(alpha = 0.5f),
                unfocusedContainerColor = Color(0xFF1A1A1A).copy(alpha = 0.5f)
            )
        )

        // Show correct answer after submit if wrong
        if (isSubmitted && !isCorrect && correctText != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF03DAC6).copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Correct answer:", color = Color(0xFF03DAC6), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(correctText, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// =====================================================================
//  3. TAP PAIRS — Match two columns by tapping (Duolingo matching)
//     Uses: options = left side items (index 0, 2, 4...)
//                     right side items (index 1, 3, 5...)
//           correctText = pipe-separated matched pairs "A|1,B|2,C|3"
// =====================================================================

data class TapPairItem(
    val text: String,
    val side: Int, // 0 = left, 1 = right
    val pairIndex: Int
)

@Composable
fun TapPairsLayout(
    options: List<String>,
    selectedAnswer: String?,
    isSubmitted: Boolean,
    isCorrect: Boolean,
    onAnswerChanged: (String) -> Unit
) {
    // Parse options into left/right columns
    // options format: [leftA, rightA, leftB, rightB, ...]
    val leftItems = options.filterIndexed { i, _ -> i % 2 == 0 }
    val rightItems = options.filterIndexed { i, _ -> i % 2 == 1 }

    // Shuffled display order
    val shuffledLeft = remember(options) { leftItems.shuffled() }
    val shuffledRight = remember(options) { rightItems.shuffled() }

    // Track matched pairs and current selection
    val matchedPairs = selectedAnswer?.split(",")?.filter { it.contains("|") } ?: emptyList()
    val matchedLeft = matchedPairs.map { it.split("|")[0] }.toSet()
    val matchedRight = matchedPairs.mapNotNull { it.split("|").getOrNull(1) }.toSet()

    var selectedLeft by remember { mutableStateOf<String?>(null) }
    var selectedRight by remember { mutableStateOf<String?>(null) }

    // When both sides selected, create a pair
    LaunchedEffect(selectedLeft, selectedRight) {
        if (selectedLeft != null && selectedRight != null) {
            val newPair = "${selectedLeft}|${selectedRight}"
            val current = if (selectedAnswer.isNullOrBlank()) newPair
            else "$selectedAnswer,$newPair"
            onAnswerChanged(current)
            selectedLeft = null
            selectedRight = null
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Tap to match the pairs",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                shuffledLeft.forEach { item ->
                    val isMatched = item in matchedLeft
                    val isActive = item == selectedLeft

                    PairCard(
                        text = item,
                        isMatched = isMatched,
                        isActive = isActive,
                        isSubmitted = isSubmitted,
                        isCorrectPair = isMatched && isSubmitted && isCorrect,
                        onClick = {
                            if (!isSubmitted && !isMatched) {
                                selectedLeft = if (isActive) null else item
                            }
                        }
                    )
                }
            }

            // Right column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                shuffledRight.forEach { item ->
                    val isMatched = item in matchedRight
                    val isActive = item == selectedRight

                    PairCard(
                        text = item,
                        isMatched = isMatched,
                        isActive = isActive,
                        isSubmitted = isSubmitted,
                        isCorrectPair = isMatched && isSubmitted && isCorrect,
                        onClick = {
                            if (!isSubmitted && !isMatched) {
                                selectedRight = if (isActive) null else item
                            }
                        }
                    )
                }
            }
        }

        // Matched pairs display
        if (matchedPairs.isNotEmpty()) {
            Text(
                text = "${matchedPairs.size} pairs matched",
                color = Color(0xFF03DAC6).copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PairCard(
    text: String,
    isMatched: Boolean,
    isActive: Boolean,
    isSubmitted: Boolean,
    isCorrectPair: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = when {
            isSubmitted && isMatched && isCorrectPair -> Color(0xFF03DAC6).copy(alpha = 0.2f)
            isSubmitted && isMatched && !isCorrectPair -> Color(0xFFCF6679).copy(alpha = 0.2f)
            isMatched -> Color(0xFF6200EE).copy(alpha = 0.2f)
            isActive -> Color(0xFF6200EE).copy(alpha = 0.4f)
            else -> Color(0xFF1A1A1A)
        },
        animationSpec = tween(200),
        label = "pair_bg"
    )

    val scale by animateFloatAsState(
        targetValue = when {
            isActive -> 1.05f
            isMatched -> 0.95f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = 0.6f),
        label = "pair_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(enabled = !isSubmitted && !isMatched, onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(12.dp),
        border = if (isActive) CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF6200EE))
        ) else null
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = when {
                    isMatched -> Color.White.copy(alpha = 0.5f)
                    else -> Color.White.copy(alpha = 0.9f)
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

// =====================================================================
//  FLOW ROW — simple wrap-content horizontal layout
//  (FlowRow is in compose foundation in newer versions, but we provide
//   a simple fallback for broader compatibility)
// =====================================================================
// Note: FlowRow is available from compose foundation 1.7.0+ 
// which should be available in this project's compose-multiplatform 1.7.0
