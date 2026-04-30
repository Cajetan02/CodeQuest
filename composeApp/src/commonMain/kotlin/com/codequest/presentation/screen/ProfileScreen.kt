package com.codequest.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codequest.presentation.viewmodel.ProfileViewModel
import org.koin.compose.koinInject
import androidx.compose.material.icons.filled.Edit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateHome: () -> Unit,
    onNavigateLeaderboard: () -> Unit,
    onNavigateAuth: () -> Unit,
    viewModel: ProfileViewModel = koinInject()
) {
    val stats by viewModel.stats.collectAsState()
    val achievements by viewModel.achievements.collectAsState()
    
    var showEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    val displayName = stats?.fullName ?: "Loading..."
    
    if (showEditDialog) {
        EditProfileDialog(
            currentName = displayName,
            currentAvatar = stats?.avatarUrl,
            onDismiss = { showEditDialog = false },
            onSave = { newName, newAvatar ->
                if (newName.isNotBlank() && newName != displayName) viewModel.updateName(newName)
                if (newAvatar != stats?.avatarUrl) viewModel.updateAvatar(newAvatar)
                showEditDialog = false
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.ExtraBold) },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontWeight = FontWeight.Bold) },
                    selected = false,
                    onClick = onNavigateHome
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Leaderboard, contentDescription = "Leaderboard") },
                    label = { Text("Rank", fontWeight = FontWeight.Bold) },
                    selected = false,
                    onClick = onNavigateLeaderboard
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile", fontWeight = FontWeight.Bold) },
                    selected = true,
                    onClick = { }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            // Premium Avatar Image with Gradient Ring
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.size(112.dp).clip(CircleShape).background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    if (stats?.avatarUrl.isNullOrBlank()) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.size(72.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(stats?.avatarUrl ?: "🤖", fontSize = 64.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            val displayName = stats?.fullName ?: "Loading..."
            Text(displayName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            Text("Joined Recently", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha=0.6f))
            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(24.dp), 
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total XP", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.7f))
                        Text("${stats?.totalXp ?: 0}", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                    }
                    Divider(modifier = Modifier.height(60.dp).width(1.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.2f))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Streak", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.7f))
                        Text("${stats?.currentStreak ?: 0} \uD83D\uDD25", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.ExtraBold) // Fire emoji
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            Text("Achievements", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(achievements) { achievement ->
                    Card(
                        modifier = Modifier.aspectRatio(1f),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(8.dp)) {
                            Text(achievement.title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = { viewModel.logout(onSuccess = onNavigateAuth) },
                modifier = Modifier.fillMaxWidth().height(50.dp).padding(bottom = 8.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Text("Logout", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EditProfileDialog(
    currentName: String,
    currentAvatar: String?,
    onDismiss: () -> Unit,
    onSave: (name: String, avatar: String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var avatar by remember { mutableStateOf(currentAvatar ?: "🤖") }
    
    val predefinedAvatars = listOf("🤖", "👨‍💻", "👩‍💻", "🐱‍💻", "🦊", "🚀", "🧑‍🚀", "🧙‍♂️")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display Name") }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Choose Avatar:")
                Spacer(modifier = Modifier.height(8.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(140.dp)
                ) {
                    items(predefinedAvatars) { av ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(if (av == avatar) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { avatar = av },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(av, fontSize = 24.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name, avatar) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
