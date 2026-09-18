package com.sumsokol.umphakathi.ui.community

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sumsokol.umphakathi.domain.model.Community
import com.sumsokol.umphakathi.domain.model.CommunityType
import com.sumsokol.umphakathi.ui.report.CommunityTypeBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    searchQuery: String,
    onCommunityClick: (String) -> Unit,
    viewModel: CommunityViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }
    
    val filteredCommunities = remember(uiState.communities, searchQuery) {
        if (searchQuery.isBlank()) uiState.communities else {
            uiState.communities.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true) ||
                it.location?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }

    if (showCreateDialog) {
        var name by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var selectedType by remember { mutableStateOf(CommunityType.GEOGRAPHIC) }
        var location by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create New Community") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Community Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Text("Community Type", style = MaterialTheme.typography.titleSmall)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        CommunityType.entries.forEach { type ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedType = type }
                                    .padding(vertical = 4.dp)
                            ) {
                                RadioButton(
                                    selected = selectedType == type,
                                    onClick = { selectedType = type }
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = type.name.lowercase().replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank() && description.isNotBlank()) {
                            viewModel.createCommunity(name, description, selectedType, location)
                            showCreateDialog = false
                        }
                    },
                    enabled = name.isNotBlank() && description.isNotBlank()
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.GroupAdd, contentDescription = "Create Community")
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredCommunities) { community ->
                    CommunityCard(community = community, onClick = { onCommunityClick(community.id) })
                }
            }
        }
    }
}

@Composable
fun CommunityCard(community: Community, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(community.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(2.dp))
                    CommunityTypeBadge(type = community.type)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.People, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(Modifier.width(2.dp))
                    Text("${community.memberCount}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                community.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            community.location?.let { loc ->
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, Modifier.size(12.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(Modifier.width(2.dp))
                    Text(loc, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}
