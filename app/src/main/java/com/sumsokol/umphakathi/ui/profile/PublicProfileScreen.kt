package com.sumsokol.umphakathi.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sumsokol.umphakathi.domain.model.ReportStatus
import com.sumsokol.umphakathi.ui.report.IncidentCard
import com.sumsokol.umphakathi.ui.report.ReportsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicProfileScreen(
    userId: String,
    searchQuery: String,
    onBack: () -> Unit,
    onReportClick: (String) -> Unit,
    onCommentClick: (String) -> Unit,
    onImageClick: (List<String>, Int) -> Unit,
    reportsViewModel: ReportsViewModel = viewModel()
) {
    val uiState by reportsViewModel.uiState.collectAsStateWithLifecycle()
    
    // Filter by user AND search query
    val userReports = remember(uiState.reports, userId, searchQuery) {
        uiState.reports.filter { 
            (it.reporterId == userId || it.reporterName.contains(userId, ignoreCase = true)) &&
            (searchQuery.isBlank() || 
             it.title.contains(searchQuery, ignoreCase = true) || 
             it.description.contains(searchQuery, ignoreCase = true))
        }
    }

    val totalReports = userReports.size
    val resolvedReports = userReports.count { it.status == ReportStatus.RESOLVED }
    val pendingReports = totalReports - resolvedReports

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = paddingValues.calculateTopPadding() + 16.dp, bottom = paddingValues.calculateBottomPadding() + 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ProfileCard {
                    ProfileHeader(
                        name = if (userReports.isNotEmpty()) userReports.first().reporterName else "User $userId",
                        handle = "@reporter",
                        badgeText = "Verified Community Reporter"
                    )
                    
                    ProfileStatsRow(
                        stats = listOf(
                            ProfileStatData(totalReports.toString(), "Total Reports"),
                            ProfileStatData(resolvedReports.toString(), "Resolved"),
                            ProfileStatData(pendingReports.toString(), "Pending")
                        )
                    )
                    Spacer(Modifier.height(24.dp))
                }
            }

            item {
                Text(
                    text = "Report Activity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            if (userReports.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No reports made by this user yet.", color = MaterialTheme.colorScheme.outline)
                    }
                }
            } else {
                items(userReports) { report ->
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
                        onClick = { onReportClick(report.id) },
                        onMeToo = { reportsViewModel.addMeToo(report.id) },
                        onComment = { id -> onCommentClick(id) },
                        onShare = { reportsViewModel.shareReport(report.id) },
                        onHashtagClick = { /* Hashtag functionality */ },
                        onImageClick = onImageClick
                    )
                }
            }
        }
    }
}
