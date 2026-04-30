package com.codequest.presentation.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

object AnimationSpecs {
    val BounceSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    val SmoothSpec = tween<Float>(
        durationMillis = 300,
        easing = FastOutSlowInEasing
    )

    val XpFillSpec = tween<Float>(
        durationMillis = 800,
        easing = FastOutSlowInEasing
    )
}
