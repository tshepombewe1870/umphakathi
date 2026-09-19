package com.sumsokol.umphakathi.ui.organization

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
import com.sumsokol.umphakathi.domain.model.Organization
import com.sumsokol.umphakathi.domain.model.OrganizationType

@Composable
fun OrganizationScreen(
    searchQuery: String,
    onOrganizationClick: (String) -> Unit,
    viewModel: OrganizationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    val filteredOrganizations = remember(uiState.organizations, searchQuery) {
        if (searchQuery.isBlank()) uiState.organizations else {
            uiState.organizations.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.description?.contains(searchQuery, ignoreCase = true) == true ||
                it.serviceAreas.any { area -> area.contains(searchQuery, ignoreCase = true) }
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredOrganizations) { org ->
                    OrganizationCard(
                        organization = org,
                        onClick = { onOrganizationClick(org.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun OrganizationCard(
    organization: Organization,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(organization.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (organization.verified) {
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text(
                                text = organization.organizationType.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        modifier = Modifier.height(24.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Default.CorporateFare,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    modifier = Modifier.size(28.dp)
                )
            }
            organization.description?.let { desc ->
                Spacer(Modifier.height(8.dp))
                Text(
                    desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (organization.serviceAreas.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = organization.serviceAreas.joinToString(", "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
