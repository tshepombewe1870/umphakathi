package com.sumsokol.umphakathi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalTopAppBar(
    searchQuery: String,
    onAvatarClick: () -> Unit,
    onSearch: (String) -> Unit,
    showBackButton: Boolean = false,
    onBack: () -> Unit = {}
) {
    var showNotificationDialog by remember { mutableStateOf(false) }

    if (showNotificationDialog) {
        Dialog(onDismissRequest = { showNotificationDialog = false }) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Notifications", style = MaterialTheme.typography.titleMedium)
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    Text("🔔 Report #001 status changed to IN_PROGRESS")
                    Spacer(Modifier.height(8.dp))
                    Text("📢 Your community post has been shared 4 times")
                    Spacer(Modifier.height(16.dp))
                    TextButton(onClick = { showNotificationDialog = false }, modifier = Modifier.align(Alignment.End)) {
                        Text("Close")
                    }
                }
            }
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .statusBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showBackButton) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
            } else {
                // User Avatar
                IconButton(onClick = onAvatarClick) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = "My Account",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.width(4.dp))

            // Search Bar
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.extraLarge
                    )
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search...",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            )
                        }
                        androidx.compose.foundation.text.BasicTextField(
                            value = searchQuery,
                            onValueChange = onSearch,
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)
                        )
                    }
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onSearch("") },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.width(4.dp))

            // Notifications
            IconButton(onClick = { showNotificationDialog = true }) {
                BadgedBox(badge = { Badge { Text("2", fontSize = 10.sp) } }) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notifications", modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}
