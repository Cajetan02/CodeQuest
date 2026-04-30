package com.codequest.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.codequest.presentation.viewmodel.AuthViewModel
import org.koin.compose.koinInject
import androidx.compose.foundation.Image
import codequest.composeapp.generated.resources.Res
import codequest.composeapp.generated.resources.logo
import org.jetbrains.compose.resources.painterResource
@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = koinInject()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var isLoginMode by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(Res.drawable.logo),
                contentDescription = "CodeQuest Logo",
                modifier = Modifier.size(100.dp).padding(bottom = 16.dp)
            )
            Text(
                text = "Welcome to",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.LightGray
            )
            Text(
                text = "CodeQuest",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(bottom = 48.dp)
            )

            if (!isLoginMode) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    singleLine = true,
                    shape = MaterialTheme.shapes.large,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.DarkGray,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    enabled = !isLoading
                )
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                singleLine = true,
                shape = MaterialTheme.shapes.large,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.DarkGray,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                ),
                enabled = !isLoading
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                shape = MaterialTheme.shapes.large,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.DarkGray,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                ),
                enabled = !isLoading
            )

            if (isLoginMode) {
                Button(
                    onClick = { viewModel.signInWithEmail(email, password, onSuccess = onAuthSuccess) },
                    modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 12.dp),
                    enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text("Sign In", fontSize = MaterialTheme.typography.titleMedium.fontSize, fontWeight = FontWeight.Bold)
                }

                TextButton(
                    onClick = { isLoginMode = false },
                    enabled = !isLoading
                ) {
                    Text("Don't have an account? Sign Up", color = MaterialTheme.colorScheme.primary)
                }
            } else {
                Button(
                    onClick = { viewModel.signUpWithEmail(email, password, name, onSuccess = onAuthSuccess) },
                    modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 12.dp),
                    enabled = !isLoading && email.isNotBlank() && password.isNotBlank() && name.isNotBlank(),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text("Sign Up", fontSize = MaterialTheme.typography.titleMedium.fontSize, fontWeight = FontWeight.Bold)
                }

                TextButton(
                    onClick = { isLoginMode = true },
                    enabled = !isLoading
                ) {
                    Text("Already have an account? Sign In", color = MaterialTheme.colorScheme.primary)
                }
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            }

            if (error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = error ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
