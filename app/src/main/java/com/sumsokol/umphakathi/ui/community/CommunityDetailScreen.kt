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
import androidx.compose.foundation.clickable

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
    var showCreateNoticeDialog by remember { mutableStateOf(false) }
    val currentUserId = com.sumsokol.umphakathi.data.firebase.FirebaseDataModule.currentUserId

    if (showVolunteerDialog) {
        VolunteerOfferDialog(
            onDismiss = { showVolunteerDialog = false },
            onSubmit = { _, _ -> showVolunteerDialog = false }
        )
    }

    if (showCreateNoticeDialog) {
        var title by remember { mutableStateOf("") }
        var body by remember { mutableStateOf("") }
        var selectedType by remember { mutableStateOf(NoticeType.COMMUNITY_EVENT) }
        var location by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showCreateNoticeDialog = false },
            title = { Text("Post a Community Notice") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = body,
                        onValueChange = { body = it },
                        label = { Text("Details / Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Text("Notice Type", style = MaterialTheme.typography.titleSmall)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        NoticeType.entries.forEach { type ->
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
                                    text = type.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() },
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
                        if (title.isNotBlank() && body.isNotBlank()) {
                            viewModel.createNotice(title, body, selectedType, null, null, location)
                            showCreateNoticeDialog = false
                        }
                    },
                    enabled = title.isNotBlank() && body.isNotBlank()
                ) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateNoticeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SmallFloatingActionButton(
                    onClick = { showCreateNoticeDialog = true },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(Icons.Default.Campaign, contentDescription = "Post Notice")
                }
                ExtendedFloatingActionButton(
                    onClick = { onNewReport(communityId) },
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text("Report Issue") }
                )
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = paddingValues.calculateTopPadding(), bottom = paddingValues.calculateBottomPadding()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Community info header (reusing profile components)
                uiState.community?.let { community ->
                    item {
                        Box(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
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
                                        text = if (uiState.isMember) "Joined" else "Join Community",
                                        icon = if (uiState.isMember) Icons.Default.Check else Icons.Default.PersonAdd,
                                        onClick = { 
                                            if (uiState.isMember) viewModel.leaveCommunity() else viewModel.joinCommunity()
                                        },
                                        containerColor = if (uiState.isMember) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primary,
                                        contentColor = if (uiState.isMember) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimary,
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
                    }

                    item {
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            ProfileBio(bio = community.description)
                        }
                    }

                    community.location?.let {
                        item {
                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                CommunityLocationCard(location = it)
                            }
                        }
                    }

                    item {
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
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
                }

                val isModerator = currentUserId != null && uiState.community != null && 
                                  (currentUserId == uiState.community!!.ownerId || uiState.community!!.moderatorIds.contains(currentUserId))

                val approvedNotices = uiState.notices.filter { it.status == NoticeStatus.APPROVED }
                val pendingNotices = uiState.notices.filter { it.status == NoticeStatus.PENDING_APPROVAL }

                // Moderator Notice Approval Queue
                if (isModerator && pendingNotices.isNotEmpty()) {
                    item {
                        Text(
                            text = "Pending Notice Approval (${pendingNotices.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
                        )
                    }
                    items(pendingNotices) { notice ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            NoticeCard(
                                notice = notice,
                                onApprove = { viewModel.approveNotice(notice.id) },
                                onReject = { viewModel.rejectNotice(notice.id) }
                            )
                        }
                    }
                }

                // Active Notices Board
                if (approvedNotices.isNotEmpty()) {
                    item {
                        Text(
                            text = "Notice Board & Announcements",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
                        )
                    }
                    items(approvedNotices) { notice ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            NoticeCard(notice = notice)
                        }
                    }
                }

                if (uiState.posts.isNotEmpty()) {
                    item {
                        Text(
                            text = "Community Reports",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
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

@Composable
fun NoticeCard(
    notice: Notice,
    onApprove: (() -> Unit)? = null,
    onReject: (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = when (notice.type) {
                NoticeType.PLANNED_OUTAGE -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                NoticeType.PROTEST_OR_MARCH -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f)
                NoticeType.COMMUNITY_EVENT -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when (notice.type) {
                        NoticeType.PLANNED_OUTAGE -> Icons.Default.Warning
                        NoticeType.PROTEST_OR_MARCH -> Icons.Default.Campaign
                        NoticeType.COMMUNITY_EVENT -> Icons.Default.Event
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = notice.type.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "By ${notice.creatorName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(notice.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(notice.body, style = MaterialTheme.typography.bodyMedium)
            
            notice.locationDescription?.let {
                if (it.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
            
            if (onApprove != null && onReject != null) {
                Spacer(Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onReject) {
                        Text("Reject", color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = onApprove) {
                        Text("Approve")
                    }
                }
            }
        }
    }
}
