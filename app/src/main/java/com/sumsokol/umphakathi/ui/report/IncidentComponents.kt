package com.sumsokol.umphakathi.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.sumsokol.umphakathi.ReportDetailNav
import com.sumsokol.umphakathi.domain.model.*
import java.time.Duration
import java.time.Instant

@Composable
fun IncidentCard(
    reportId: String,
    title: String,
    description: String,
    status: ReportStatus,
    category: ReportCategory,
    urgency: Urgency,
    submittedAt: Instant,
    neighborhood: String?,
    meTooCount: Int,
    commentCount: Int = 0,
    volunteerCount: Int = 0,
    shareCount: Int = 0,
    authorName: String = "Anonymous",
    communityName: String? = null,
    imageUrls: List<String> = emptyList(),
    onClick: (String) -> Unit,
    onMeToo: () -> Unit,
    onComment: (String) -> Unit = {},
    onVolunteer: () -> Unit = {},
    onShare: () -> Unit = {},
    onHashtagClick: () -> Unit = {},
    onImageClick: (List<String>, Int) -> Unit = { _, _ -> }
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        val statusColor: Color = getStatusColor(status)
        LinearProgressIndicator(
            progress = { getStatusProgress(status) },
            modifier = Modifier.fillMaxWidth().height(4.dp),
            color = statusColor,
            trackColor = statusColor.copy(alpha = 0.1f)
        )
        // Make the content body clickable for navigation, completely separating it from the Action Row at the bottom
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Identity Row with Avatar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onClick(authorName) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize(),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.width(8.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onClick(authorName) }
                ) {
                    Text(
                        text = authorName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (!communityName.isNullOrBlank()) {
                        Text(
                            text = communityName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                IconButton(onClick = { /* More actions */ }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.MoreVert, "More", modifier = Modifier.size(16.dp))
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick(reportId) }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryChip(category = category)
                    UrgencyChip(urgency = urgency)
                    Spacer(Modifier.weight(1f))
                    neighborhood?.let {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, Modifier.size(12.dp), tint = MaterialTheme.colorScheme.outline)
                            Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Image preview if included (IN BETWEEN heading and description)
                if (imageUrls.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(imageUrls) { index, url ->
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(100.dp) // Slightly larger preview
                                    .clip(MaterialTheme.shapes.small)
                                    .clickable { onImageClick(imageUrls, index) },
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Text(
                        text = "See More",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Action Row is now structurally completely outside the clickable content area, making navigation interference physically impossible
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = timeAgo(submittedAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.weight(1f)
            )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionStatChip(
                        icon = Icons.Default.BackHand,
                        value = meTooCount,
                        onClick = onMeToo
                    )
                    ActionStatChip(
                        icon = Icons.AutoMirrored.Filled.Comment,
                        value = commentCount,
                        onClick = { onComment(reportId) }
                    )
                    ActionStatChip(
                        icon = Icons.Default.VolunteerActivism,
                        value = volunteerCount,
                        onClick = onVolunteer
                    )
                    ActionStatChip(
                        icon = Icons.Default.AlternateEmail,
                        value = 0,
                        onClick = onHashtagClick
                    )
                    ActionStatChip(
                        icon = Icons.Default.Share,
                        value = shareCount,
                        onClick = onShare
                    )
                }
        }
    }
}

@Composable
fun ActionStatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: Int,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = MaterialTheme.shapes.small,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                null,
                Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (value > 0) {
                Spacer(Modifier.width(6.dp))
                Text(
                    text = value.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CategoryChip(category: ReportCategory) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.small
    ) {
        Box(
            modifier = Modifier.padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = getCategoryIcon(category),
                contentDescription = getCategoryLabel(category),
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

fun getCategoryLabel(category: ReportCategory): String = when (category) {
    ReportCategory.WATER_SEWAGE -> "Water / Sewage"
    else -> category.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
}

fun getCategoryIcon(category: ReportCategory): androidx.compose.ui.graphics.vector.ImageVector = when (category) {
    ReportCategory.ABUSE -> Icons.Default.Shield
    ReportCategory.VIOLENCE -> Icons.Default.Report
    ReportCategory.MISSING_PERSON -> Icons.Default.Search
    ReportCategory.MEDICAL_EMERGENCY -> Icons.Default.LocalHospital
    ReportCategory.INFRASTRUCTURE -> Icons.Default.Construction
    ReportCategory.WATER_SEWAGE -> Icons.Default.WaterDrop
    ReportCategory.FIRE -> Icons.Default.LocalFireDepartment
    ReportCategory.NATURAL_DISASTER -> Icons.Default.Thunderstorm
    ReportCategory.CRIME -> Icons.Default.Security
    ReportCategory.UNSAFE_ENVIRONMENT -> Icons.Default.Warning
    ReportCategory.COMMUNITY_EMERGENCY -> Icons.Default.Emergency
    ReportCategory.OTHER -> Icons.AutoMirrored.Filled.Help
}

@Composable
fun UrgencyChip(urgency: Urgency) {
    val chipColor: Color = when (urgency) {
        Urgency.CRITICAL -> Color(0xFFD32F2F)
        Urgency.HIGH -> Color(0xFFE64A19)
        Urgency.MEDIUM -> Color(0xFFFFA000)
        Urgency.LOW -> Color(0xFF388E3C)
    }
    Surface(
        color = chipColor.copy(alpha = 0.15f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = urgency.name,
            style = MaterialTheme.typography.labelSmall,
            color = chipColor,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun StatusChip(status: ReportStatus) {
    val label: String
    val color: Color
    when (status) {
        ReportStatus.SUBMITTED -> { label = "Submitted"; color = Color(0xFF1976D2) }
        ReportStatus.UNDER_REVIEW -> { label = "Under Review"; color = Color(0xFFFFA000) }
        ReportStatus.ESCALATED -> { label = "Escalated"; color = Color(0xFFE64A19) }
        ReportStatus.IN_PROGRESS -> { label = "In Progress"; color = Color(0xFF388E3C) }
        ReportStatus.RESOLVED -> { label = "Resolved"; color = Color(0xFF00695C) }
        ReportStatus.ARCHIVED -> { label = "Archived"; color = Color(0xFF757575) }
    }
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

fun getStatusProgress(status: ReportStatus): Float = when (status) {
    ReportStatus.SUBMITTED -> 0.1f
    ReportStatus.UNDER_REVIEW -> 0.3f
    ReportStatus.ESCALATED -> 0.5f
    ReportStatus.IN_PROGRESS -> 0.7f
    ReportStatus.RESOLVED -> 1.0f
    ReportStatus.ARCHIVED -> 1.0f
}

fun getStatusColor(status: ReportStatus): Color = when (status) {
    ReportStatus.SUBMITTED -> Color(0xFF1976D2)
    ReportStatus.UNDER_REVIEW -> Color(0xFFFFA000)
    ReportStatus.ESCALATED -> Color(0xFFE64A19)
    ReportStatus.IN_PROGRESS -> Color(0xFF388E3C)
    ReportStatus.RESOLVED -> Color(0xFF00695C)
    ReportStatus.ARCHIVED -> Color(0xFF757575)
}

fun timeAgo(instant: Instant): String {
    val duration = Duration.between(instant, Instant.now())
    return when {
        duration.toMinutes() < 1 -> "just now"
        duration.toMinutes() < 60 -> "${duration.toMinutes()}m ago"
        duration.toHours() < 24 -> "${duration.toHours()}h ago"
        duration.toDays() < 7 -> "${duration.toDays()}d ago"
        else -> "${duration.toDays() / 7}w ago"
    }
}

@Composable
fun TimelineEventRow(event: AuditEvent) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(32.dp)) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(40.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.action.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = timeAgo(event.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
            if (event.metadata.isNotEmpty()) {
                Text(
                    text = event.metadata.entries.joinToString { "${it.key}: ${it.value}" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun OfficialUpdateCard(update: OfficialUpdate) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Verified, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(4.dp))
                Text(update.organizationName, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text(timeAgo(update.createdAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
            Spacer(Modifier.height(4.dp))
            Text(update.message, style = MaterialTheme.typography.bodySmall)
            update.statusUpdate?.let { status ->
                Spacer(Modifier.height(8.dp))
                StatusChip(status = status)
            }
        }
    }
}

@Composable
fun VolunteerOfferCard(offer: VolunteerOffer) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.VolunteerActivism,
                null,
                Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = offer.userName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = offer.resourceType.name.replace('_', ' ').lowercase()
                        .replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                offer.note?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            val statusColor: Color = when(offer.status) {
                VolunteerStatus.ACCEPTED -> Color(0xFF388E3C)
                VolunteerStatus.OFFERED -> Color(0xFF1976D2)
                else -> Color.Gray
            }
            Surface(
                color = statusColor.copy(alpha = 0.15f),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    offer.status.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun VolunteerOfferDialog(
    onDismiss: () -> Unit,
    onSubmit: (List<ResourceType>, String) -> Unit
) {
    val selectedResources = remember { mutableStateListOf<ResourceType>() }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Offer Help / Volunteer") },
        text = {
            Column {
                Text("What can you provide? (Select all that apply)", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(12.dp))
                ResourceType.entries.forEach { type ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (selectedResources.contains(type)) {
                                    selectedResources.remove(type)
                                } else {
                                    selectedResources.add(type)
                                }
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = selectedResources.contains(type),
                            onCheckedChange = { checked ->
                                if (checked) selectedResources.add(type) else selectedResources.remove(type)
                            }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            type.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Additional note (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(selectedResources.toList(), note) },
                enabled = selectedResources.isNotEmpty()
            ) { Text("Offer Help") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun CommunityTypeBadge(type: CommunityType) {
    val icon = when (type) {
        CommunityType.GEOGRAPHIC -> Icons.Default.Map
        CommunityType.ORGANIZATION -> Icons.Default.Business
        CommunityType.OTHER -> Icons.Default.Category
    }
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.small
    ) {
        Icon(
            imageVector = icon,
            contentDescription = type.name,
            modifier = Modifier.padding(4.dp).size(14.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}
