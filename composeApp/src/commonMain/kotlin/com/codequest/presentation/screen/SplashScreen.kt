package com.codequest.presentation.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codequest.presentation.theme.AnimationSpecs
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import codequest.composeapp.generated.resources.Res
import codequest.composeapp.generated.resources.logo

import org.koin.compose.koinInject
import com.codequest.domain.repository.AuthRepository

@Composable
fun SplashScreen(
    onSplashFinished: (Boolean) -> Unit,
    authRepository: AuthRepository = koinInject()
) {
    val currentUserId by authRepository.currentUserId.collectAsState(initial = null)
    var isAnimStarted by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isAnimStarted) 1f else 0.5f,
        animationSpec = AnimationSpecs.BounceSpec,
        label = "SplashScale"
    )

    LaunchedEffect(Unit) {
        isAnimStarted = true
        delay(1500)
        onSplashFinished(currentUserId != null)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(Res.drawable.logo),
                contentDescription = "CodeQuest Logo",
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "CodeQuest",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}
