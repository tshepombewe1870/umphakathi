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
    onUserClick: (String) -> Unit,
    onCommunityClick: (String) -> Unit,
    onCommentClick: (String) -> Unit,
    onImageClick: (List<String>, Int) -> Unit,
    reportsViewModel: ReportsViewModel = viewModel()
) {
    val uiState by reportsViewModel.uiState.collectAsStateWithLifecycle()
    var selectedFilter by remember { mutableStateOf("All") }
    
    // Filter by user AND search query AND interactive metric tab
    val userReports = remember(uiState.reports, userId, searchQuery, selectedFilter) {
        uiState.reports.filter { 
            val matchesUser = (it.reporterId == userId || it.reporterName.contains(userId, ignoreCase = true))
            val matchesSearch = (searchQuery.isBlank() || 
                                it.title.contains(searchQuery, ignoreCase = true) || 
                                it.description.contains(searchQuery, ignoreCase = true))
            val matchesFilter = when (selectedFilter) {
                "Resolved" -> it.status == ReportStatus.RESOLVED
                "Pending" -> it.status != ReportStatus.RESOLVED
                else -> true
            }
            matchesUser && matchesSearch && matchesFilter
        }
    }

    // Get true count independent of active filter view
    val totalCount = remember(uiState.reports, userId) {
        uiState.reports.count { it.reporterId == userId || it.reporterName.contains(userId, ignoreCase = true) }
    }
    val resolvedCount = remember(uiState.reports, userId) {
        uiState.reports.count { (it.reporterId == userId || it.reporterName.contains(userId, ignoreCase = true)) && it.status == ReportStatus.RESOLVED }
    }
    val pendingCount = totalCount - resolvedCount

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
            contentPadding = PaddingValues(top = paddingValues.calculateTopPadding() + 4.dp, bottom = paddingValues.calculateBottomPadding() + 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    ProfileCard {
                        ProfileHeader(
                            name = if (userReports.isNotEmpty()) userReports.first().reporterName else "User $userId",
                            handle = "@reporter",
                            badgeText = "Verified Community Reporter"
                        )
                        
                        ProfileStatsRow(
                            stats = listOf(
                                ProfileStatData(totalCount.toString(), "All"),
                                ProfileStatData(resolvedCount.toString(), "Resolved"),
                                ProfileStatData(pendingCount.toString(), "Pending")
                            ),
                            selectedFilter = selectedFilter,
                            onFilterSelect = { selectedFilter = it }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            item {
                Text(
                    text = "Report Activity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            if (userReports.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (selectedFilter == "All") "No reports made by this user yet." else "No $selectedFilter reports found.",
                            color = MaterialTheme.colorScheme.outline)
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
                        authorId = report.reporterId,
                        authorName = report.reporterName,
                        communityId = report.communityId,
                        communityName = report.communityName,
                        isAnonymous = report.isAnonymous,
                        imageUrls = report.imageUrls,
                        onClick = { onReportClick(report.id) },
                        onUserClick = { uId -> onUserClick(uId) },
                        onCommunityClick = { cId -> onCommunityClick(cId) },
                        onMeToo = { reportsViewModel.addMeToo(report.id) },
                        onComment = { id -> onCommentClick(id) },
                        onShare = { reportsViewModel.shareReport(report.id) },
                        onHashtagClick = { /* Hashtag functionality */ },
                        onImageClick = onImageClick,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}
