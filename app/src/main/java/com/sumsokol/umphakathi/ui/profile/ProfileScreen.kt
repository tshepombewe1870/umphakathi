package com.sumsokol.umphakathi.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sumsokol.umphakathi.data.firebase.FirebaseSeeder
import com.sumsokol.umphakathi.data.firebase.FirebaseDataModule
import com.sumsokol.umphakathi.ui.auth.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToPublicProfile: (String) -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    val userId = authViewModel.getCurrentUserId() ?: "anonymous"
    val profileUiState by profileViewModel.uiState.collectAsStateWithLifecycle()
    
    // Find the persona for immediate UI feedback while Firestore loads
    val currentPersona = remember(userId) {
        authViewModel.personas.find { it.id == userId }
    }

    val scope = rememberCoroutineScope()
    var seedingInProgress by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account Settings", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = paddingValues.calculateTopPadding() + 4.dp, bottom = paddingValues.calculateBottomPadding() + 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ProfileCard {
                    val user = profileUiState.user
                    
                    // Use Firestore user if available, otherwise fallback to Persona demo data
                    val displayName = user?.username ?: currentPersona?.name ?: "Loading..."
                    val handle = "@${user?.id ?: currentPersona?.id ?: userId}"
                    val role = user?.role ?: currentPersona?.role ?: "Member"
                    val community = user?.communityName ?: currentPersona?.community ?: "Community"

                    ProfileHeader(
                        name = displayName,
                        handle = handle,
                        badgeText = "$role • $community"
                    )
                    
                    Spacer(Modifier.height(4.dp))
                    
                    Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                        ActionButton(
                            text = "View Public Profile",
                            icon = Icons.Default.Person,
                            onClick = { onNavigateToPublicProfile(userId) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                        OutlinedButton(
                            onClick = {
                                seedingInProgress = true
                                scope.launch {
                                    FirebaseSeeder.seed(FirebaseDataModule.firestore)
                                    seedingInProgress = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !seedingInProgress
                        ) {
                            if (seedingInProgress) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp))
                            } else {
                                Icon(Icons.Default.Backup, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Migrate/Seed Data to Firestore")
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }

            item {
                // Privacy card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, null, Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Your Privacy Protection", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Text(
                                "Your absolute identity is kept anonymous on critical hazard submissions. Departments see aggregated reports for fast allocation.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                // Settings options
                ProfileCard {
                    Column {
                        Text("Preferences", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
                        HorizontalDivider()
                        SettingRowItem(icon = Icons.Default.Notifications, label = "Notifications Feed")
                        HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                        SettingRowItem(icon = Icons.Default.Language, label = "App Language")
                        HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                        SettingRowItem(icon = Icons.Default.Security, label = "Security & Encryption")
                        HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                        SettingRowItem(
                            icon = Icons.AutoMirrored.Filled.Logout,
                            label = "Sign Out Account", 
                            tint = MaterialTheme.colorScheme.error,
                            onClick = { authViewModel.logout() }
                        )
                    }
                }
            }
        }
    }
}
