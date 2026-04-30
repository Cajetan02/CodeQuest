package com.codequest.presentation.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codequest.domain.model.Question
import com.codequest.domain.model.QuestionType
import com.codequest.presentation.components.*
import com.codequest.presentation.viewmodel.QuizViewModel
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    lessonId: String,
    onNavigateBack: () -> Unit,
    onLessonComplete: (score: Int, maxScore: Int) -> Unit,
    viewModel: QuizViewModel = koinInject()
) {
    val questions by viewModel.questions.collectAsState()
    val currentIndex by viewModel.currentQuestionIndex.collectAsState()
    val lesson by viewModel.lesson.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    var score by remember { mutableIntStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var isSubmitted by remember { mutableStateOf(false) }
    var hasSeenTheory by remember { mutableStateOf(false) }

    // ===========================================
    //  Animation state
    // ===========================================
    var hearts by remember { mutableIntStateOf(3) }
    var streak by remember { mutableIntStateOf(0) }
    var showConfetti by remember { mutableStateOf(false) }
    var showXpPopup by remember { mutableStateOf(false) }
    var lastXpGain by remember { mutableIntStateOf(0) }
    var showIncorrectShake by remember { mutableStateOf(false) }
    var showExplanation by remember { mutableStateOf(false) }

    // Reset confetti after animation
    LaunchedEffect(showConfetti) {
        if (showConfetti) {
            delay(2500)
            showConfetti = false
        }
    }

    // Reset XP popup
    LaunchedEffect(showXpPopup) {
        if (showXpPopup) {
            delay(1200)
            showXpPopup = false
        }
    }

    // Reset shake
    LaunchedEffect(showIncorrectShake) {
        if (showIncorrectShake) {
            delay(500)
            showIncorrectShake = false
        }
    }

    LaunchedEffect(lessonId) {
        viewModel.loadQuestions(lessonId)
    }

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(errorMessage ?: "") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }

    if (questions.isEmpty() || lesson == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val theoryContent = lesson?.content
    if (!theoryContent.isNullOrBlank() && !hasSeenTheory) {
        TheoryScreen(
            title = lesson?.title ?: "Theory",
            content = theoryContent,
            onStartQuiz = { hasSeenTheory = true },
            onNavigateBack = onNavigateBack
        )
        return
    }

    // Game over — no hearts left
    if (hearts <= 0) {
        GameOverScreen(
            score = score,
            maxScore = questions.sumOf { it.xpReward },
            onRetry = {
                hearts = 3
                score = 0
                streak = 0
                selectedAnswer = null
                isSubmitted = false
                viewModel.loadQuestions(lessonId)
            },
            onQuit = onNavigateBack
        )
        return
    }

    val currentQuestion = questions[currentIndex]
    val progress = ((currentIndex + 1).toFloat() / questions.size)

    val isCorrect = remember(selectedAnswer, currentQuestion) {
        when (currentQuestion.type) {
            QuestionType.MULTIPLE_CHOICE, QuestionType.TRUE_FALSE, QuestionType.CODE_SNIPPET -> {
                selectedAnswer == currentQuestion.options.getOrNull(currentQuestion.correctAnswerIndex ?: -1)
            }
            QuestionType.FILL_IN_BLANK, QuestionType.LISTEN_TYPE -> {
                selectedAnswer?.trim().equals(currentQuestion.correctText?.trim(), ignoreCase = true)
            }
            QuestionType.CODE_ORDER -> {
                selectedAnswer == currentQuestion.correctOrder?.joinToString("\n")
            }
            QuestionType.MATCHING, QuestionType.TAP_PAIRS -> {
                selectedAnswer == currentQuestion.correctText
            }
            QuestionType.WORD_BANK -> {
                selectedAnswer?.trim().equals(currentQuestion.correctText?.trim(), ignoreCase = true)
            }
        }
    }

    // Shake offset for incorrect answers
    val shakeOffset = rememberShakeAnimation(showIncorrectShake)

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Glowing progress bar
                                GlowingProgressBar(
                                    progress = progress,
                                    checkpointCount = questions.size,
                                    currentCheckpoint = currentIndex,
                                    modifier = Modifier.weight(1f)
                                )
                                // Hearts display
                                HeartsDisplay(
                                    currentHearts = hearts,
                                    maxHearts = 3
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) { Icon(Icons.Default.Close, "Close") }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            titleContentColor = MaterialTheme.colorScheme.onBackground,
                            navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                        )
                    )

                    // Streak indicator
                    AnimatedVisibility(
                        visible = streak >= 2,
                        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            StreakFlame(streak = streak)
                        }
                    }
                }
            },
            bottomBar = {
                AnimatedQuizBottomBar(
                    isSubmitted = isSubmitted,
                    hasSelected = !selectedAnswer.isNullOrBlank(),
                    isCorrect = isCorrect,
                    streak = streak,
                    xpReward = currentQuestion.xpReward,
                    explanation = if (showExplanation) currentQuestion.explanation else null,
                    onCheck = {
                        isSubmitted = true
                        if (isCorrect) {
                            showConfetti = true
                            showXpPopup = true
                            lastXpGain = currentQuestion.xpReward
                            streak++
                        } else {
                            showIncorrectShake = true
                            showExplanation = currentQuestion.explanation != null
                            streak = 0
                            hearts--
                        }
                    },
                    onContinue = {
                        if (isCorrect) {
                            score += currentQuestion.xpReward
                        }
                        showExplanation = false
                        if (currentIndex < questions.size - 1) {
                            viewModel.nextQuestion()
                            isSubmitted = false
                            selectedAnswer = null
                        } else {
                            val maxScore = questions.sumOf { it.xpReward }
                            viewModel.submitLesson(lessonId, score, maxScore) {
                                onLessonComplete(score, maxScore)
                            }
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                AnimatedContent(
                    targetState = currentIndex,
                    label = "Question Transition",
                    transitionSpec = {
                        (slideInHorizontally { width -> width } + fadeIn())
                            .togetherWith(slideOutHorizontally { width -> -width } + fadeOut())
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .graphicsLayer { translationX = shakeOffset }
                ) { targetIndex ->
                    val animatedQuestion = questions[targetIndex]
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Question number badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        Color(0xFF6200EE).copy(alpha = 0.2f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Q${targetIndex + 1}/${questions.size}",
                                    color = Color(0xFF6200EE),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(
                                        Color(0xFF03DAC6).copy(alpha = 0.15f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "+${animatedQuestion.xpReward} XP",
                                    color = Color(0xFF03DAC6),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            // Question type label
                            Text(
                                text = formatQuestionType(animatedQuestion.type),
                                color = Color.White.copy(alpha = 0.3f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Question prompt
                        Text(
                            animatedQuestion.prompt,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        
                        // Code snippet if present
                        if (!animatedQuestion.codeSnippet.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF0D1117)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    // Code header bar
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(modifier = Modifier.size(10.dp).background(Color(0xFFFF5F56), shape = RoundedCornerShape(50)))
                                        Box(modifier = Modifier.size(10.dp).background(Color(0xFFFFBD2E), shape = RoundedCornerShape(50)))
                                        Box(modifier = Modifier.size(10.dp).background(Color(0xFF27C93F), shape = RoundedCornerShape(50)))
                                    }
                                    Text(
                                        text = animatedQuestion.codeSnippet!!,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFF79C0FF),
                                        style = MaterialTheme.typography.bodyMedium,
                                        lineHeight = 22.sp
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Question type layouts
                        when (animatedQuestion.type) {
                            QuestionType.MULTIPLE_CHOICE, QuestionType.TRUE_FALSE,
                            QuestionType.CODE_SNIPPET, QuestionType.MATCHING -> {
                                MultipleChoiceGrid(
                                    options = animatedQuestion.options,
                                    selectedOption = selectedAnswer,
                                    isSubmitted = isSubmitted,
                                    correctOptionStr = animatedQuestion.options.getOrNull(
                                        animatedQuestion.correctAnswerIndex ?: -1
                                    ),
                                    onOptionSelected = { if (!isSubmitted) selectedAnswer = it }
                                )
                            }
                            QuestionType.FILL_IN_BLANK -> {
                                FillInBlankLayout(
                                    selectedAnswer = selectedAnswer,
                                    isSubmitted = isSubmitted,
                                    isCorrect = isCorrect,
                                    onAnswerChanged = { selectedAnswer = it }
                                )
                            }
                            QuestionType.CODE_ORDER -> {
                                CodeOrderLayout(
                                    options = animatedQuestion.options,
                                    selectedAnswer = selectedAnswer,
                                    isSubmitted = isSubmitted,
                                    isCorrect = isCorrect,
                                    onAnswerChanged = { selectedAnswer = it }
                                )
                            }
                            QuestionType.WORD_BANK -> {
                                WordBankLayout(
                                    options = animatedQuestion.options,
                                    selectedAnswer = selectedAnswer,
                                    isSubmitted = isSubmitted,
                                    isCorrect = isCorrect,
                                    onAnswerChanged = { selectedAnswer = it }
                                )
                            }
                            QuestionType.LISTEN_TYPE -> {
                                ListenTypeLayout(
                                    prompt = animatedQuestion.prompt,
                                    selectedAnswer = selectedAnswer,
                                    isSubmitted = isSubmitted,
                                    isCorrect = isCorrect,
                                    correctText = animatedQuestion.correctText,
                                    onAnswerChanged = { selectedAnswer = it }
                                )
                            }
                            QuestionType.TAP_PAIRS -> {
                                TapPairsLayout(
                                    options = animatedQuestion.options,
                                    selectedAnswer = selectedAnswer,
                                    isSubmitted = isSubmitted,
                                    isCorrect = isCorrect,
                                    onAnswerChanged = { selectedAnswer = it }
                                )
                            }
                        }
                    }
                }

                // XP popup overlay
                AnimatedVisibility(
                    visible = showXpPopup,
                    enter = scaleIn(spring(dampingRatio = 0.5f)) + fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    XpPopup(
                        xpAmount = lastXpGain,
                        isVisible = showXpPopup
                    )
                }
            }
        }

        // Confetti overlay (above everything)
        ConfettiBurst(isActive = showConfetti)
    }
}

// =====================================================================
//  ANIMATED BOTTOM BAR — enhanced with colors, feedback, and explanations
// =====================================================================

@Composable
fun AnimatedQuizBottomBar(
    isSubmitted: Boolean,
    hasSelected: Boolean,
    isCorrect: Boolean,
    streak: Int,
    xpReward: Int,
    explanation: String?,
    onCheck: () -> Unit,
    onContinue: () -> Unit
) {
    val targetColor by animateColorAsState(
        targetValue = when {
            isSubmitted && isCorrect -> Color(0xFF03DAC6).copy(alpha = 0.15f)
            isSubmitted && !isCorrect -> Color(0xFFCF6679).copy(alpha = 0.15f)
            else -> MaterialTheme.colorScheme.background
        },
        animationSpec = tween(300),
        label = "bottom_bar_color"
    )

    Surface(
        shadowElevation = 12.dp,
        color = targetColor
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            // Explanation card for incorrect answers
            AnimatedVisibility(
                visible = explanation != null && isSubmitted && !isCorrect,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1A1A2E)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            "💡 Explanation",
                            color = Color(0xFFFFD600),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            explanation ?: "",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSubmitted) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isCorrect) "✨ Correct!" else "✗ Incorrect",
                                color = if (isCorrect) Color(0xFF03DAC6) else Color(0xFFCF6679),
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp
                            )
                        }
                        if (isCorrect && streak > 1) {
                            Text(
                                text = "🔥 ${streak}x streak bonus!",
                                color = Color(0xFFFF6D00),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (isCorrect) {
                            Text(
                                text = "+$xpReward XP",
                                color = Color(0xFF03DAC6).copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
                
                Button(
                    onClick = if (isSubmitted) onContinue else onCheck,
                    enabled = hasSelected,
                    modifier = Modifier.height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSubmitted && isCorrect) Color(0xFF03DAC6)
                        else if (isSubmitted) Color(0xFFCF6679)
                        else Color(0xFF6200EE),
                        contentColor = if (isSubmitted && isCorrect) Color.Black
                        else Color.White
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        if (isSubmitted) "Continue" else "Check",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

// =====================================================================
//  IMPROVED MULTIPLE CHOICE GRID — with animations
// =====================================================================

@Composable
fun MultipleChoiceGrid(
    options: List<String>,
    selectedOption: String?,
    isSubmitted: Boolean,
    correctOptionStr: String?,
    onOptionSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        options.forEachIndexed { index, option ->
            val isSelected = option == selectedOption
            val isCorrectOption = option == correctOptionStr

            val bgColor by animateColorAsState(
                targetValue = when {
                    isSubmitted && isSelected && isCorrectOption -> Color(0xFF03DAC6).copy(alpha = 0.25f)
                    isSubmitted && isSelected && !isCorrectOption -> Color(0xFFCF6679).copy(alpha = 0.25f)
                    isSubmitted && isCorrectOption -> Color(0xFF03DAC6).copy(alpha = 0.12f)
                    isSelected -> Color(0xFF6200EE).copy(alpha = 0.25f)
                    else -> Color(0xFF1A1A1A)
                },
                animationSpec = tween(200),
                label = "option_bg_$index"
            )

            val borderColor by animateColorAsState(
                targetValue = when {
                    isSubmitted && isSelected && isCorrectOption -> Color(0xFF03DAC6)
                    isSubmitted && isSelected && !isCorrectOption -> Color(0xFFCF6679)
                    isSubmitted && isCorrectOption -> Color(0xFF03DAC6).copy(alpha = 0.5f)
                    isSelected -> Color(0xFF6200EE)
                    else -> Color(0xFF3D3D4D)
                },
                animationSpec = tween(200),
                label = "option_border_$index"
            )

            val scale by animateFloatAsState(
                targetValue = if (isSelected && !isSubmitted) 1.02f else 1f,
                animationSpec = spring(dampingRatio = 0.6f),
                label = "option_scale_$index"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { scaleX = scale; scaleY = scale },
                onClick = { onOptionSelected(option) },
                enabled = !isSubmitted,
                colors = CardDefaults.cardColors(containerColor = bgColor),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(borderColor)
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Option letter badge
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                borderColor.copy(alpha = 0.2f),
                                RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ('A' + index).toString(),
                            color = borderColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Text(
                        option,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                    // Result indicator
                    if (isSubmitted) {
                        Text(
                            text = when {
                                isCorrectOption -> "✓"
                                isSelected && !isCorrectOption -> "✗"
                                else -> ""
                            },
                            color = if (isCorrectOption) Color(0xFF03DAC6) else Color(0xFFCF6679),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// =====================================================================
//  GAME OVER SCREEN
// =====================================================================

@Composable
private fun GameOverScreen(
    score: Int,
    maxScore: Int,
    onRetry: () -> Unit,
    onQuit: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A12)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "💔",
                fontSize = 64.sp
            )
            Text(
                text = "No hearts left!",
                color = Color(0xFFCF6679),
                fontSize = 28.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "You earned $score / $maxScore XP",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EE)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Try Again", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            OutlinedButton(
                onClick = onQuit,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4A4A5A)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Quit", color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
            }
        }
    }
}

// =====================================================================
//  HELPERS
// =====================================================================

private fun formatQuestionType(type: QuestionType): String = when (type) {
    QuestionType.MULTIPLE_CHOICE -> "Multiple Choice"
    QuestionType.FILL_IN_BLANK -> "Fill in Blank"
    QuestionType.CODE_ORDER -> "Code Order"
    QuestionType.TRUE_FALSE -> "True/False"
    QuestionType.CODE_SNIPPET -> "Code Snippet"
    QuestionType.MATCHING -> "Matching"
    QuestionType.WORD_BANK -> "Word Bank"
    QuestionType.LISTEN_TYPE -> "Listen & Type"
    QuestionType.TAP_PAIRS -> "Tap Pairs"
}

// Keep existing layouts for backwards compat
@Composable
fun FillInBlankLayout(
    selectedAnswer: String?,
    isSubmitted: Boolean,
    isCorrect: Boolean,
    onAnswerChanged: (String) -> Unit
) {
    OutlinedTextField(
        value = selectedAnswer ?: "",
        onValueChange = onAnswerChanged,
        enabled = !isSubmitted,
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
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
}

@Composable
fun CodeOrderLayout(
    options: List<String>,
    selectedAnswer: String?,
    isSubmitted: Boolean,
    isCorrect: Boolean,
    onAnswerChanged: (String) -> Unit
) {
    val selectedItems = selectedAnswer?.split("\n")?.filter { it.isNotEmpty() } ?: emptyList()
    val availableItems = options.filter { it !in selectedItems }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Answer area
        Card(
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 100.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    isSubmitted && isCorrect -> Color(0xFF03DAC6).copy(alpha = 0.12f)
                    isSubmitted && !isCorrect -> Color(0xFFCF6679).copy(alpha = 0.12f)
                    else -> Color(0xFF1A1A1A).copy(alpha = 0.6f)
                }
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (selectedItems.isEmpty()) {
                    Text(
                        "Tap lines below to arrange them here",
                        color = Color.White.copy(alpha = 0.4f)
                    )
                } else {
                    selectedItems.forEachIndexed { index, item ->
                        Card(
                            onClick = {
                                if (!isSubmitted) {
                                    val newSelected = selectedItems.toMutableList()
                                    newSelected.removeAt(index)
                                    onAnswerChanged(newSelected.joinToString("\n"))
                                }
                            },
                            enabled = !isSubmitted,
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF6200EE).copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "${index + 1}",
                                    color = Color(0xFF6200EE),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    item,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        if (availableItems.isNotEmpty()) {
            Text(
                "Available lines:",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.5f)
            )
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                availableItems.forEach { item ->
                    OutlinedCard(
                        onClick = {
                            if (!isSubmitted) {
                                val newSelected = selectedItems + item
                                onAnswerChanged(newSelected.joinToString("\n"))
                            }
                        },
                        enabled = !isSubmitted,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF4A4A5A))
                        )
                    ) {
                        Text(
                            item,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}

// Keep TheoryScreen and QuizBottomBar as aliases for backwards compat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TheoryScreen(
    title: String,
    content: String,
    onStartQuiz: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.Close, "Close") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.background
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = onStartQuiz,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
                    ) {
                        Text(
                            "Start Quiz",
                            fontSize = MaterialTheme.typography.titleMedium.fontSize,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text(
                text = "📖 Study Material",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF6200EE),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            com.codequest.presentation.components.MarkdownText(
                text = content,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
