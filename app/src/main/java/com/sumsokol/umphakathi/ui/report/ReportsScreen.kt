package com.sumsokol.umphakathi.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sumsokol.umphakathi.domain.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    searchQuery: String,
    onReportClick: (String) -> Unit,
    onCrisisClick: (String) -> Unit,
    onUserClick: (String) -> Unit,
    onCommentClick: (String) -> Unit,
    onVolunteerClick: (String) -> Unit,
    onImageClick: (List<String>, Int) -> Unit,
    onNewReport: () -> Unit,
    viewModel: ReportsViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Comprehensive multi-item search filter directly linked
    val filteredReports = remember(uiState.reports, searchQuery) {
        if (searchQuery.isBlank()) uiState.reports else {
            uiState.reports.filter {
                it.title.contains(searchQuery, ignoreCase = true) || 
                it.description.contains(searchQuery, ignoreCase = true) ||
                it.reporterName.contains(searchQuery, ignoreCase = true) ||
                it.category.name.contains(searchQuery, ignoreCase = true) ||
                it.communityName?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // Active Crises section
                if (uiState.crises.isNotEmpty() && searchQuery.isEmpty()) {
                    item {
                        Text(
                            text = "Active Crises",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            uiState.crises.forEach { crisis ->
                                CrisisCard(crisis = crisis, onClick = { onCrisisClick(crisis.id) })
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                    item { HorizontalDivider() }
                }

                // Reports section
                if (filteredReports.isNotEmpty()) {
                    items(filteredReports) { report ->
                        IncidentCard(
                            reportId = report.id,
                            title = report.title,
                            description = report.description,
                            status = report.status,
                            category = report.category,
                            urgency = report.urgency,
                            submittedAt = report.submittedAt,
                            neighborhood = report.incidentLocation.neighborhood,
                            meTooCount = report.meTooCount,
                            commentCount = report.commentCount,
                            volunteerCount = report.volunteerCount,
                            shareCount = report.shareCount,
                            authorName = report.reporterName,
                            communityName = report.communityName,
                            imageUrls = report.imageUrls,
                            onClick = { id -> onReportClick(id) },
                            onUserClick = { _ -> onUserClick(report.reporterId) },
                            onCommunityClick = { _ -> report.communityId?.let { cId -> onReportClick("COMM_ID:$cId") } }, // Signal community navigate
                            onMeToo = { viewModel.addMeToo(report.id) },
                            onComment = { id -> onCommentClick(id) },
                            onVolunteer = { onVolunteerClick(report.id) },
                            onShare = { viewModel.shareReport(report.id) },
                            onHashtagClick = { /* Hashtag functionality */ },
                            onImageClick = onImageClick
                        )
                    }
                } else if (searchQuery.isNotEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No results for \"$searchQuery\"", color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
        }
        
        // Floating action button for new report (moved from top bar)
        ExtendedFloatingActionButton(
            onClick = onNewReport,
            icon = { Icon(Icons.Default.Add, null) },
            text = { Text("Report") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }
}

@Composable
fun CrisisCard(crisis: Crisis, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(260.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (crisis.potentialHarm) {
                PotentialHarm.EXTREME -> MaterialTheme.colorScheme.errorContainer
                PotentialHarm.HIGH -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = when (crisis.urgency) {
                        Urgency.CRITICAL -> MaterialTheme.colorScheme.error
                        Urgency.HIGH -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.secondary
                    },
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = crisis.status.name.replace('_', ' '),
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(Modifier.weight(1f))
                CategoryChip(category = crisis.category)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = crisis.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            crisis.incidentLocation.city?.let {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, Modifier.size(12.dp))
                    Spacer(Modifier.width(2.dp))
                    Text(it, style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatChip(icon = Icons.Default.Description, value = crisis.reportCount.toString(), label = "reports")
                StatChip(icon = Icons.Default.People, value = crisis.meTooCount.toString(), label = "me too")
            }
        }
    }
}

@Composable
fun StatChip(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(12.dp))
        Spacer(Modifier.width(2.dp))
        Text("$value $label", style = MaterialTheme.typography.labelSmall)
    }
}
