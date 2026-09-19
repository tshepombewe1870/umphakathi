package com.sumsokol.umphakathi.ui.organization

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sumsokol.umphakathi.domain.model.*
import com.sumsokol.umphakathi.ui.profile.*
import com.sumsokol.umphakathi.ui.report.IncidentCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizationDetailScreen(
    organizationId: String,
    onBack: () -> Unit,
    onReportClick: (String) -> Unit,
    onUserClick: (String) -> Unit,
    onCommunityClick: (String) -> Unit,
    viewModel: OrganizationDetailViewModel = viewModel(key = organizationId, factory = OrganizationDetailViewModel.factory(organizationId))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.organization?.name ?: "Organization Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                uiState.organization?.let { org ->
                    item {
                        Box(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp)) {
                            ProfileCard {
                                ProfileHeader(
                                    name = org.name,
                                    handle = org.organizationType.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() },
                                    badgeText = if (org.verified) "Verified Entity" else "Registered Entity",
                                    badgeIcon = if (org.verified) Icons.Default.Verified else Icons.Default.CorporateFare
                                )
                                
                                ProfileStatsRow(
                                    stats = listOf(
                                        ProfileStatData(uiState.allCount.toString(), "All"),
                                        ProfileStatData(uiState.assignedCount.toString(), "Assigned"),
                                        ProfileStatData(uiState.resolvedCount.toString(), "Resolved"),
                                        ProfileStatData(uiState.activeCount.toString(), "Active")
                                    ),
                                    selectedFilter = uiState.selectedFilter,
                                    onFilterSelect = { filterLabel ->
                                        viewModel.setFilter(filterLabel)
                                    }
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }

                    org.description?.let { desc ->
                        item {
                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                ProfileBio(bio = desc)
                            }
                        }
                    }

                    if (org.serviceAreas.isNotEmpty()) {
                        item {
                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary)
                                            Spacer(Modifier.width(8.dp))
                                            Text("Service Mandate Areas", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(Modifier.height(8.dp))
                                        Text(org.serviceAreas.joinToString(", "), style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                }

                if (uiState.reports.isNotEmpty()) {
                    item {
                        Text(
                            text = "Assigned Incident Reports",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
                        )
                    }
                }

                if (uiState.reports.isEmpty()) {
                    item {
                        Box(
                            Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.TaskAlt, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                                Spacer(Modifier.height(8.dp))
                                Text("No active cases assigned to this organization.",
                                    color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                } else {
                    items(uiState.reports) { report ->
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
                            authorId = report.reporterId,
                            authorName = report.reporterName,
                            communityId = report.communityId,
                            communityName = report.communityName,
                            isAnonymous = report.isAnonymous,
                            imageUrls = report.imageUrls,
                            onClick = { id -> onReportClick(id) },
                            onUserClick = { uId -> onUserClick(uId) },
                            onCommunityClick = { cId -> onCommunityClick(cId) },
                            onMeToo = { },
                            onComment = { id -> onReportClick(id) },
                            onVolunteer = { },
                            onShare = { },
                            onHashtagClick = { },
                            onImageClick = { _, _ -> },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}
