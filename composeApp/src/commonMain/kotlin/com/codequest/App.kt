package com.codequest

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.codequest.presentation.navigation.Route
import com.codequest.presentation.screen.*
import com.codequest.presentation.theme.CodeQuestTheme

import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize

@Composable
fun App() {
    CodeQuestTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = Route.Splash
            ) {
                composable<Route.Splash> {
                    SplashScreen(
                        onSplashFinished = { isLoggedIn ->
                            if (isLoggedIn) {
                                navController.navigate(Route.Home) {
                                    popUpTo<Route.Splash> { inclusive = true }
                                }
                            } else {
                                navController.navigate(Route.Auth) {
                                    popUpTo<Route.Splash> { inclusive = true }
                                }
                            }
                        }
                    )
                }
                composable<Route.Auth> {
                    AuthScreen(
                        onAuthSuccess = {
                            navController.navigate(Route.Home) {
                                popUpTo<Route.Auth> { inclusive = true }
                            }
                        }
                    )
                }
                composable<Route.Home> {
                    HomeScreen(
                        onLanguageSelected = { id ->
                            navController.navigate(Route.Language(id))
                        },
                        onNavigateProfile = {
                            navController.navigate(Route.Profile) {
                                popUpTo<Route.Home>()
                                launchSingleTop = true
                            }
                        },
                        onNavigateLeaderboard = {
                            navController.navigate(Route.Leaderboard) {
                                popUpTo<Route.Home>()
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable<Route.Language> { backStackEntry ->
                    val route = backStackEntry.toRoute<Route.Language>()
                    LanguageScreen(
                        languageId = route.languageId,
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onNavigateLesson = { lessonId ->
                            navController.navigate(Route.Quiz(lessonId))
                        }
                    )
                }
                composable<Route.Quiz> { backStackEntry ->
                    val route = backStackEntry.toRoute<Route.Quiz>()
                    QuizScreen(
                        lessonId = route.lessonId,
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onLessonComplete = { score, maxScore ->
                            navController.navigate(Route.LessonComplete(score, maxScore)) {
                                popUpTo<Route.Quiz> { inclusive = true }
                            }
                        }
                    )
                }
                composable<Route.LessonComplete> { backStackEntry ->
                    val route = backStackEntry.toRoute<Route.LessonComplete>()
                    LessonCompleteScreen(
                        score = route.score,
                        maxScore = route.maxScore,
                        onContinue = {
                            navController.popBackStack()
                        }
                    )
                }
                composable<Route.Leaderboard> {
                    LeaderboardScreen(
                        onNavigateHome = {
                            navController.popBackStack(Route.Home, inclusive = false)
                        },
                        onNavigateProfile = {
                            navController.navigate(Route.Profile) {
                                popUpTo<Route.Home>()
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable<Route.Profile> {
                    ProfileScreen(
                        onNavigateHome = {
                            navController.popBackStack(Route.Home, inclusive = false)
                        },
                        onNavigateLeaderboard = {
                            navController.navigate(Route.Leaderboard) {
                                popUpTo<Route.Home>()
                                launchSingleTop = true
                            }
                        },
                        onNavigateAuth = {
                            navController.navigate(Route.Auth) {
                                popUpTo<Route.Home> { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}

expect fun getPlatformName(): String
