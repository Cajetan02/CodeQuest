package com.codequest.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.codequest.domain.model.Lesson
import com.codequest.presentation.viewmodel.LanguageViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageScreen(
    languageId: String,
    onNavigateBack: () -> Unit,
    onNavigateLesson: (String) -> Unit,
    viewModel: LanguageViewModel = koinInject()
) {
    val lessons by viewModel.lessons.collectAsState()
    val completedLessonIds by viewModel.completedLessonIds.collectAsState()
    val isLanguageCompleted by viewModel.isLanguageCompleted.collectAsState()

    LaunchedEffect(languageId) {
        viewModel.loadLessons(languageId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lessons") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = 32.dp)
        ) {
            if (isLanguageCompleted) {
                item {
                    LanguageCompletedBadge()
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
            itemsIndexed(lessons) { index, lesson ->
                val isCompleted = completedLessonIds.contains(lesson.id)
                val isUnlocked = index == 0 || completedLessonIds.contains(lessons[index - 1].id)
                
                LessonNode(
                    lesson = lesson,
                    isUnlocked = isUnlocked,
                    isCompleted = isCompleted,
                    onClick = { if (isUnlocked && !isCompleted) onNavigateLesson(lesson.id) }
                )
                
                if (index < lessons.size - 1) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(60.dp)
                            .background(
                                if (isUnlocked) MaterialTheme.colorScheme.primary 
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun LessonNode(
    lesson: Lesson,
    isUnlocked: Boolean,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isCompleted -> MaterialTheme.colorScheme.secondary
        isUnlocked -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val icon = when {
        isCompleted -> Icons.Default.Check
        isUnlocked -> Icons.Default.Star
        else -> Icons.Default.Lock
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(backgroundColor)
                .clickable(enabled = isUnlocked, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isUnlocked || isCompleted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = lesson.title,
            fontWeight = FontWeight.Bold,
            color = if (isUnlocked) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun LanguageCompletedBadge() {
    Card(
        modifier = Modifier.padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Language Completed!", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSecondary)
                Text("Incredible job mastering these concepts.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondary.copy(alpha=0.8f))
            }
        }
    }
}
