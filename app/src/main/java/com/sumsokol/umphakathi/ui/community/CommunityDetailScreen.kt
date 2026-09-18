package com.sumsokol.umphakathi.ui.community

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
import com.sumsokol.umphakathi.ui.report.CommunityTypeBadge
import com.sumsokol.umphakathi.ui.report.VolunteerOfferDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityDetailScreen(
    communityId: String,
    onBack: () -> Unit,
    onPostClick: (String) -> Unit,
    onCommentClick: (String) -> Unit,
    onImageClick: (List<String>, Int) -> Unit,
    onNewReport: (String) -> Unit,
    viewModel: CommunityDetailViewModel = viewModel(key = communityId, factory = CommunityDetailViewModel.factory(communityId))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showVolunteerDialog by remember { mutableStateOf(false) }

    if (showVolunteerDialog) {
        VolunteerOfferDialog(
            onDismiss = { showVolunteerDialog = false },
            onSubmit = { _, _ -> showVolunteerDialog = false }
        )
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onNewReport(communityId) },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Report Issue") }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Community info header (reusing profile components)
                uiState.community?.let { community ->
                    item {
                        ProfileCard {
                            ProfileHeader(
                                name = community.name,
                                handle = community.location ?: "Local Community",
                                badgeText = "${community.memberCount} members",
                                badgeIcon = Icons.Default.Groups
                            )
                            
                            ProfileStatsRow(
                                stats = listOf(
                                    ProfileStatData(uiState.posts.size.toString(), "Reports"),
                                    ProfileStatData(uiState.posts.count { it.status == PostStatus.RESOLVED }.toString(), "Resolved"),
                                    ProfileStatData(uiState.posts.count { it.status != PostStatus.RESOLVED }.toString(), "Active")
                                )
                            )
                            
                            Spacer(Modifier.height(16.dp))
                            
                            Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                                ActionButton(
                                    text = "Join Community",
                                    icon = Icons.Default.PersonAdd,
                                    onClick = { /* Join */ },
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(Modifier.width(8.dp))
                                ActionButton(
                                    text = "Share",
                                    icon = Icons.Default.Share,
                                    onClick = { /* Share */ },
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(Modifier.height(24.dp))
                        }
                    }

                    item {
                        ProfileBio(bio = community.description)
                    }

                    community.location?.let {
                        item {
                            CommunityLocationCard(location = it)
                        }
                    }

                    item {
                        CommunityRulesCard(
                            rules = listOf(
                                "Be respectful to other community members.",
                                "Only report verified local incidents.",
                                "Do not spam the feed with unrelated content.",
                                "Protect the privacy of individuals in reports."
                            )
                        )
                    }
                }

                if (uiState.posts.isNotEmpty()) {
                    item {
                        Text(
                            text = "Community Reports",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                if (uiState.posts.isEmpty()) {
                    item {
                        Box(
                            Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Forum, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                                Spacer(Modifier.height(8.dp))
                                Text("No posts yet. Be the first to report an issue!",
                                    color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                } else {
                    items(uiState.posts) { post ->
                        IncidentCard(
                            reportId = post.reportId ?: post.id,
                            title = post.title,
                            description = post.body,
                            status = if (post.status == PostStatus.RESOLVED) ReportStatus.RESOLVED else ReportStatus.IN_PROGRESS,
                            category = ReportCategory.COMMUNITY_EMERGENCY,
                            urgency = Urgency.MEDIUM,
                            submittedAt = post.createdAt,
                            neighborhood = null,
                            meTooCount = post.meTooCount,
                            commentCount = post.commentCount,
                            volunteerCount = post.volunteerCount,
                            shareCount = 0, // Community posts might not have shares yet in model
                            authorName = post.authorName,
                            communityName = post.communityName,
                            imageUrls = post.imageUrls,
                            onClick = { id -> onPostClick(id) },
                            onMeToo = { /* Corroborate */ },
                            onComment = { id -> onCommentClick(id) },
                            onVolunteer = { showVolunteerDialog = true },
                            onShare = { viewModel.shareReport(post.reportId ?: post.id) },
                            onHashtagClick = { /* Hashtag functionality */ },
                            onImageClick = onImageClick
                        )
                    }
                }
            }
        }
    }
}
