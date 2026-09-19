package com.sumsokol.umphakathi.ui.report

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.sumsokol.umphakathi.domain.model.*
import java.time.Instant

enum class DetailTab(val label: String, val icon: ImageVector) {
    OVERVIEW("Overview", Icons.Outlined.Description),
    ACTIVITY("Activity", Icons.Outlined.Timeline),
    COMMUNITY("Community", Icons.Outlined.Groups)
}

// ───────────────────────────── Header ─────────────────────────────

@Composable
fun ReportHeaderSection(report: Report, onImageClick: (Int) -> Unit, onMoreClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Status-colored progress accent — same visual language as the report list.
        LinearProgressIndicator(
            progress = { getStatusProgress(report.status) },
            modifier = Modifier.fillMaxWidth().height(3.dp),
            color = getStatusColor(report.status),
            trackColor = getStatusColor(report.status).copy(alpha = 0.12f)
        )

        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = "Avatar",
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    val displayName = if (report.isAnonymous) "Anonymous Reporter" else report.reporterName
                    Text(displayName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    if (!report.isAnonymous && !report.communityName.isNullOrBlank()) {
                        Text(report.communityName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    }
                }
                StatusChip(status = report.status)
                Spacer(Modifier.width(4.dp))
                IconButton(onClick = onMoreClick, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.MoreVert, "More actions")
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(report.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(timeAgo(report.submittedAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)

            if (report.imageUrls.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    report.imageUrls.forEachIndexed { index, url ->
                        AsyncImage(
                            model = url,
                            contentDescription = "Report Image $index",
                            modifier = Modifier
                                .size(if (report.imageUrls.size == 1) 220.dp else 140.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onImageClick(index) },
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CategoryChip(category = report.category)
                UrgencyChip(urgency = report.urgency)
            }
        }
    }
}

// ─────────────────────────── Primary actions ───────────────────────────

@Composable
fun PrimaryActionRow(
    hasMeTood: Boolean,
    onMeToo: () -> Unit,
    onVolunteer: () -> Unit,
    onShare: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onMeToo,
            modifier = Modifier.weight(1f),
            colors = if (hasMeTood) ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)) else ButtonDefaults.buttonColors(),
            shape = MaterialTheme.shapes.large,
            contentPadding = PaddingValues(vertical = 10.dp)
        ) {
            Icon(Icons.Default.ThumbUp, null, Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(if (hasMeTood) "Corroborated" else "Me Too", style = MaterialTheme.typography.labelLarge, maxLines = 1)
        }
        OutlinedButton(
            onClick = onVolunteer,
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.large,
            contentPadding = PaddingValues(vertical = 10.dp)
        ) {
            Icon(Icons.Default.VolunteerActivism, null, Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Help", style = MaterialTheme.typography.labelLarge, maxLines = 1)
        }
        OutlinedButton(
            onClick = onShare,
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.large,
            contentPadding = PaddingValues(vertical = 10.dp)
        ) {
            Icon(Icons.Default.Share, null, Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Share", style = MaterialTheme.typography.labelLarge, maxLines = 1)
        }
    }
}

// ─────────────────────────────── Tab row ───────────────────────────────

@Composable
fun DetailTabRow(selected: DetailTab, onSelect: (DetailTab) -> Unit) {
    TabRow(selectedTabIndex = selected.ordinal) {
        DetailTab.entries.forEach { tab ->
            Tab(
                selected = selected == tab,
                onClick = { onSelect(tab) },
                text = { Text(tab.label, style = MaterialTheme.typography.labelLarge) },
                icon = { Icon(tab.icon, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }
    }
}

// ───────────────────────────── Overview tab ─────────────────────────────

@Composable
fun OverviewTab(report: Report, uiState: ReportDetailUiState, onStatClick: (StatType) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { StatsCard(report, onStatClick) }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Description", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(report.description, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        val loc = report.incidentLocation
        if (loc.source != LocationSource.NOT_PROVIDED) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(4.dp))
                            Text("Incident Location", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(8.dp))
                        val locationText = listOfNotNull(
                            loc.addressDescription?.takeIf { it.isNotBlank() },
                            loc.neighborhood?.takeIf { it.isNotBlank() },
                            loc.city?.takeIf { it.isNotBlank() }
                        ).joinToString(", ").ifBlank { "Location shared" }
                        Text(locationText, style = MaterialTheme.typography.bodyMedium)
                        if (report.locationVisibility == LocationVisibility.PUBLIC_APPROXIMATE) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Approximate location shown for privacy",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }

        item {
            Text("Clear Next Steps", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("In addition to this report, you may want to take these actions:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        items(getNextStepsForCategory(report.category)) { step ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(step.title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text(step.description, style = MaterialTheme.typography.bodySmall)
                    }
                    step.hotline?.let {
                        IconButton(onClick = { /* Call hotline */ }) {
                            Icon(Icons.Default.Phone, "Call", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(64.dp)) } // clears the FAB-equivalent affordance on short content
    }
}

@Composable
private fun StatsCard(report: Report, onStatClick: (StatType) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StatColumn(report.meTooCount, "Me Too") { onStatClick(StatType.ME_TOO) }
            StatDivider()
            StatColumn(report.volunteerCount, "Volunteers") { onStatClick(StatType.VOLUNTEERS) }
            StatDivider()
            StatColumn(report.shareCount, "Shares") { onStatClick(StatType.SHARES) }
            report.peopleAffected?.let {
                StatDivider()
                StatColumn(it, "Affected", clickable = false) {}
            }
        }
    }
}

@Composable
private fun StatDivider() {
    VerticalDivider(modifier = Modifier.height(36.dp), color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun StatColumn(value: Int, label: String, clickable: Boolean = true, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        enabled = clickable,
        color = Color.Transparent,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
        ) {
            Text(value.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}

// ───────────────────────────── Activity tab ─────────────────────────────

private sealed interface ActivityItem {
    val createdAt: Instant
    data class UpdateItem(val update: OfficialUpdate) : ActivityItem {
        override val createdAt get() = update.createdAt
    }
    data class AuditItem(val event: AuditEvent) : ActivityItem {
        override val createdAt get() = event.createdAt
    }
}

@Composable
fun ActivityTab(officialUpdates: List<OfficialUpdate>, auditEvents: List<AuditEvent>) {
    val merged = remember(officialUpdates, auditEvents) {
        (officialUpdates.map { ActivityItem.UpdateItem(it) } + auditEvents.map { ActivityItem.AuditItem(it) })
            .sortedByDescending { it.createdAt }
    }

    if (merged.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            EmptyStatMessage("No activity yet — updates and status changes will appear here.")
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        itemsIndexed(merged) { index, item ->
            val isLast = index == merged.lastIndex
            when (item) {
                is ActivityItem.UpdateItem -> ActivityTimelineRow(
                    icon = Icons.Default.Verified,
                    accentColor = MaterialTheme.colorScheme.primary,
                    title = item.update.organizationName,
                    timestamp = timeAgo(item.update.createdAt),
                    body = item.update.message,
                    trailingChip = item.update.statusUpdate?.let { { StatusChip(status = it) } },
                    isLast = isLast
                )
                is ActivityItem.AuditItem -> {
                    val metadataText = if (item.event.metadata.isNotEmpty()) {
                        when (item.event.action) {
                            AuditAction.OFFICIAL_UPDATE_ADDED -> null // already shown as its own UpdateItem row
                            else -> item.event.metadata.entries.joinToString { "${it.key}: ${it.value}" }
                        }
                    } else null
                    ActivityTimelineRow(
                        icon = Icons.Default.Circle,
                        accentColor = MaterialTheme.colorScheme.outline,
                        title = item.event.action.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() },
                        timestamp = timeAgo(item.event.createdAt),
                        body = metadataText,
                        trailingChip = null,
                        isLast = isLast
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityTimelineRow(
    icon: ImageVector,
    accentColor: Color,
    title: String,
    timestamp: String,
    body: String?,
    trailingChip: (@Composable () -> Unit)?,
    isLast: Boolean
) {
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(28.dp)) {
            Surface(
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.15f),
                modifier = Modifier.size(22.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, modifier = Modifier.size(12.dp), tint = accentColor)
                }
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.padding(bottom = 20.dp).weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(timestamp, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
            body?.let {
                Spacer(Modifier.height(2.dp))
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            trailingChip?.let {
                Spacer(Modifier.height(6.dp))
                it()
            }
        }
    }
}

// ──────────────────────────── Community tab ────────────────────────────

@Composable
fun CommunityTab(
    volunteerOffers: List<VolunteerOffer>,
    comments: List<Comment>,
    onLikeOffer: (String) -> Unit,
    onCommentOnOffer: (String) -> Unit,
    onOpenComments: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { SectionHeader("Volunteer Offers", volunteerOffers.size) }
        if (volunteerOffers.isEmpty()) {
            item { EmptyStatMessage("No volunteer offers yet.") }
        } else {
            items(volunteerOffers) { offer ->
                VolunteerOfferCard(
                    offer = offer,
                    onLike = { onLikeOffer(offer.id) },
                    onComment = { onCommentOnOffer(offer.id) }
                )
            }
        }

        item {
            Spacer(Modifier.height(4.dp))
            HorizontalDivider()
            Spacer(Modifier.height(4.dp))
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .clickable(onClick = onOpenComments)
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader("Comments", comments.size)
                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
            }
        }

        if (comments.isEmpty()) {
            item { EmptyStatMessage("No comments yet. Be the first to say something.") }
        } else {
            items(comments.take(3)) { comment -> CommentPreviewRow(comment) }
            if (comments.size > 3) {
                item {
                    TextButton(onClick = onOpenComments) {
                        Text("View all ${comments.size} comments")
                    }
                }
            }
        }

        item { Spacer(Modifier.height(64.dp)) }
    }
}

@Composable
private fun CommentPreviewRow(comment: Comment) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Icon(
            Icons.Default.AccountCircle, null,
            Modifier.size(24.dp), tint = MaterialTheme.colorScheme.outline
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(comment.authorName, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(6.dp))
                Text(timeAgo(comment.createdAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
            Text(
                comment.body,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int) {
    Text("$title ($count)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
}
