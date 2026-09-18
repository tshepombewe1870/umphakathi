package com.sumsokol.umphakathi.ui.report

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sumsokol.umphakathi.domain.model.CommunityPost
import com.sumsokol.umphakathi.domain.model.ReportExperience
import com.sumsokol.umphakathi.domain.model.ReportStatus
import com.sumsokol.umphakathi.domain.model.ResourceType

/** Which single overlay is on screen — replaces five independent booleans. */
sealed interface DetailDialog {
    data object MeToo : DetailDialog
    data object Resolve : DetailDialog
    data object Volunteer : DetailDialog
    data object OfficialUpdate : DetailDialog
    data object Comments : DetailDialog
    data class Stat(val type: StatType) : DetailDialog
}

/**
 * Renders whichever [DetailDialog] is active, or nothing. Callers own the state;
 * this is purely presentational so the screen stays a single source of truth.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailDialogHost(
    activeDialog: DetailDialog?,
    uiState: ReportDetailUiState,
    onDismiss: () -> Unit,
    onConfirmMeToo: (String?) -> Unit,
    onConfirmResolve: (String) -> Unit,
    onSubmitVolunteer: (List<ResourceType>, String) -> Unit,
    onPostUpdate: (String, ReportStatus?) -> Unit,
    onAddComment: (String, String?) -> Unit
) {
    when (activeDialog) {
        DetailDialog.MeToo -> MeTooDialog(onDismiss = onDismiss, onConfirm = onConfirmMeToo)
        DetailDialog.Resolve -> ResolveDialog(onDismiss = onDismiss, onConfirm = onConfirmResolve)
        DetailDialog.Volunteer -> VolunteerOfferDialog(onDismiss = onDismiss, onSubmit = onSubmitVolunteer)
        DetailDialog.OfficialUpdate -> OfficialUpdateDialog(onDismiss = onDismiss, onSubmit = onPostUpdate)
        DetailDialog.Comments -> {
            val report = uiState.report
            if (report != null) {
                CommentBottomSheet(
                    reportId = report.id,
                    onDismiss = onDismiss,
                    comments = uiState.comments,
                    onAddComment = onAddComment
                )
            }
        }
        is DetailDialog.Stat -> {
            ModalBottomSheet(
                onDismissRequest = onDismiss,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                StatDetailContent(type = activeDialog.type, uiState = uiState, onClose = onDismiss)
            }
        }
        null -> Unit
    }
}

@Composable
private fun MeTooDialog(onDismiss: () -> Unit, onConfirm: (String?) -> Unit) {
    var description by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("I've experienced this too") },
        text = {
            Column {
                Text("You can optionally add more context about your experience:")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(description.ifBlank { null }) }) { Text("Confirm") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun ResolveDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var explanation by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mark as Resolved") },
        text = {
            Column {
                Text("How was this situation resolved?")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = explanation,
                    onValueChange = { explanation = it },
                    label = { Text("Resolution details") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(explanation) }) { Text("Mark Resolved") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun OfficialUpdateDialog(onDismiss: () -> Unit, onSubmit: (String, ReportStatus?) -> Unit) {
    var message by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf<ReportStatus?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Post Official Update") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Provide a status update or message from your organization.")
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Update Message") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                Text("Change Report Status (Optional)", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReportStatus.entries.forEach { status ->
                        FilterChip(
                            selected = selectedStatus == status,
                            onClick = { selectedStatus = if (selectedStatus == status) null else status },
                            label = { Text(status.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(message, selectedStatus) },
                enabled = message.isNotBlank()
            ) { Text("Post Update") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun StatDetailContent(
    type: StatType,
    uiState: ReportDetailUiState,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.7f)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (type) {
                    StatType.ME_TOO -> "Corroborations (${uiState.corroborations.size})"
                    StatType.VOLUNTEERS -> "Volunteer Offers (${uiState.volunteerOffers.size})"
                    StatType.SHARES -> "Shared Posts (${uiState.shares.size})"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            when (type) {
                StatType.ME_TOO -> {
                    if (uiState.corroborations.isEmpty()) {
                        item { EmptyStatMessage("No corroborations yet.") }
                    } else {
                        items(uiState.corroborations) { exp -> ExperienceItem(exp) }
                    }
                }
                StatType.VOLUNTEERS -> {
                    if (uiState.volunteerOffers.isEmpty()) {
                        item { EmptyStatMessage("No volunteer offers yet.") }
                    } else {
                        items(uiState.volunteerOffers) { offer -> VolunteerOfferCard(offer = offer) }
                    }
                }
                StatType.SHARES -> {
                    if (uiState.shares.isEmpty()) {
                        item { EmptyStatMessage("This report hasn't been shared yet.") }
                    } else {
                        items(uiState.shares) { post -> SharedPostItem(post) }
                    }
                }
            }
        }
    }
}

@Composable
fun ExperienceItem(exp: ReportExperience) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AccountCircle, null,
                    Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(exp.userName, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text(timeAgo(exp.createdAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
            exp.description?.let {
                Spacer(Modifier.height(4.dp))
                Text(it, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun SharedPostItem(post: CommunityPost) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AccountCircle, null,
                    Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(post.authorName, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text(timeAgo(post.createdAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
            Spacer(Modifier.height(4.dp))
            Text(post.title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            Text(
                post.body,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun EmptyStatMessage(message: String) {
    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(message, color = MaterialTheme.colorScheme.outline)
    }
}
