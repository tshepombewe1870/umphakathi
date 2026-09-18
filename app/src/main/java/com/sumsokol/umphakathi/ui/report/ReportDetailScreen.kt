package com.sumsokol.umphakathi.ui.report

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.sumsokol.umphakathi.domain.model.*
import kotlinx.coroutines.launch

enum class StatType { ME_TOO, VOLUNTEERS, SHARES }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    reportId: String,
    onBack: () -> Unit,
    onCrisisClick: (String) -> Unit,
    viewModel: ReportDetailViewModel = viewModel(key = reportId, factory = ReportDetailViewModel.factory(reportId))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showMeTooDialog by remember { mutableStateOf(false) }
    var showVolunteerDialog by remember { mutableStateOf(false) }
    var showResolveDialog by remember { mutableStateOf(false) }
    var meTooDescription by remember { mutableStateOf("") }
    var resolveExplanation by remember { mutableStateOf("") }
    var hasMeTood by remember { mutableStateOf(false) }
    
    var selectedStatType by remember { mutableStateOf<StatType?>(null) }
    var showStatBottomSheet by remember { mutableStateOf(false) }
    
    var selectedImageIndex by remember { mutableStateOf<Int?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    if (showStatBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showStatBottomSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            StatDetailContent(
                type = selectedStatType ?: StatType.ME_TOO,
                uiState = uiState,
                onClose = { showStatBottomSheet = false }
            )
        }
    }

    if (showMeTooDialog) {
        AlertDialog(
            onDismissRequest = { showMeTooDialog = false },
            title = { Text("I've experienced this too") },
            text = {
                Column {
                    Text("You can optionally add more context about your experience:")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = meTooDescription,
                        onValueChange = { meTooDescription = it },
                        label = { Text("Description (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.addMeToo(meTooDescription.ifBlank { null })
                    hasMeTood = true
                    showMeTooDialog = false
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showMeTooDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showResolveDialog) {
        AlertDialog(
            onDismissRequest = { showResolveDialog = false },
            title = { Text("Mark as Resolved") },
            text = {
                Column {
                    Text("How was this situation resolved?")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = resolveExplanation,
                        onValueChange = { resolveExplanation = it },
                        label = { Text("Resolution details") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resolveReport(resolveExplanation)
                    showResolveDialog = false
                }) { Text("Mark Resolved") }
            },
            dismissButton = {
                TextButton(onClick = { showResolveDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showVolunteerDialog) {
        VolunteerOfferDialog(
            onDismiss = { showVolunteerDialog = false },
            onSubmit = { types, note ->
                viewModel.submitVolunteerOffer(types, note)
                showVolunteerDialog = false
            }
        )
    }

    val report = uiState.report
    if (report != null && selectedImageIndex != null) {
        ImageSlideshowScreen(
            imageUrls = report.imageUrls,
            initialIndex = selectedImageIndex ?: 0,
            onBack = { selectedImageIndex = null }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Report Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading || report == null) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    // Header Card with User Info
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // User Info and actions header above everything else
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.AccountCircle,
                                    contentDescription = "Avatar",
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = report.reporterName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (!report.communityName.isNullOrBlank()) {
                                        Text(
                                            text = report.communityName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("More report actions selected")
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.MoreVert, "More actions")
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StatusChip(status = report.status)
                                Text(timeAgo(report.submittedAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            }
                            Spacer(Modifier.height(8.dp))
                            
                            // Heading
                            Text(report.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            
                            // Images in-between heading and description if included
                            if (report.imageUrls.isNotEmpty()) {
                                Spacer(Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    report.imageUrls.forEachIndexed { index, url ->
                                        AsyncImage(
                                            model = url,
                                            contentDescription = "Report Image $index",
                                            modifier = Modifier
                                                .size(150.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { selectedImageIndex = index },
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                CategoryChip(category = report.category)
                                UrgencyChip(urgency = report.urgency)
                            }
                        }
                    }
                }

                item {
                    // Description
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Description", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Text(report.description, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                // Location
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
                                val locationText = when (report.locationVisibility) {
                                    LocationVisibility.PRIVATE -> "Location is private"
                                    LocationVisibility.RESPONDERS_ONLY -> "Location shared with responders only"
                                    else -> listOfNotNull(
                                        loc.addressDescription,
                                        loc.neighborhood,
                                        loc.city,
                                        loc.province
                                    ).joinToString(", ").ifBlank { "Location provided" }
                                }
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
                    // Stats & Actions
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.clickable {
                                        selectedStatType = StatType.ME_TOO
                                        showStatBottomSheet = true
                                    }
                                ) {
                                    Text(report.meTooCount.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                    Text("Me Too", style = MaterialTheme.typography.labelSmall)
                                }
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.clickable {
                                        selectedStatType = StatType.VOLUNTEERS
                                        showStatBottomSheet = true
                                    }
                                ) {
                                    Text(report.volunteerCount.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                    Text("Volunteers", style = MaterialTheme.typography.labelSmall)
                                }
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.clickable {
                                        selectedStatType = StatType.SHARES
                                        showStatBottomSheet = true
                                    }
                                ) {
                                    Text(report.shareCount.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                    Text("Shares", style = MaterialTheme.typography.labelSmall)
                                }
                                report.peopleAffected?.let {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(it.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                        Text("Affected", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            
                            // Action buttons with added Tag and Share actions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { showMeTooDialog = true },
                                    modifier = Modifier.weight(1f),
                                    colors = if (hasMeTood) ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)) else ButtonDefaults.buttonColors(),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.ThumbUp, null, Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(if (hasMeTood) "Corroborated" else "Me Too", style = MaterialTheme.typography.labelSmall, maxLines = 1)
                                }
                                OutlinedButton(
                                    onClick = { showVolunteerDialog = true },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.VolunteerActivism, null, Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Help", style = MaterialTheme.typography.labelSmall, maxLines = 1)
                                }
                            }
                            
                            Spacer(Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Tagging feature coming soon!")
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.AlternateEmail, null, Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Tag", style = MaterialTheme.typography.labelSmall, maxLines = 1)
                                }
                                OutlinedButton(
                                    onClick = {
                                        viewModel.shareReport()
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Report shared to your feed!")
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.Share, null, Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Share", style = MaterialTheme.typography.labelSmall, maxLines = 1)
                                }
                            }

                            if (report.status != ReportStatus.RESOLVED && report.status != ReportStatus.ARCHIVED) {
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = { showResolveDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
                                ) {
                                    Icon(Icons.Default.CheckCircle, null, Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Mark Resolved")
                                }
                            }
                            if (report.status != ReportStatus.ESCALATED && report.crisisId == null && report.status != ReportStatus.RESOLVED) {
                                Spacer(Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = { viewModel.flagAsCrisis() },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE64A19))
                                ) {
                                    Icon(Icons.Default.Warning, null, Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Flag as Crisis")
                                }
                            }
                        }
                    }
                }

                // Official updates
                if (uiState.officialUpdates.isNotEmpty()) {
                    item {
                        Text("Official Updates (${uiState.officialUpdates.size})",
                            style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    items(uiState.officialUpdates) { update ->
                        OfficialUpdateCard(update = update)
                    }
                }

                // Timeline / Audit
                if (uiState.auditEvents.isNotEmpty()) {
                    item {
                        Text("Incident Timeline",
                            style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    items(uiState.auditEvents) { event ->
                        TimelineEventRow(event = event)
                    }
                }

                // Volunteer offers
                if (uiState.volunteerOffers.isNotEmpty()) {
                    item {
                        Text("Volunteer Offers (${uiState.volunteerOffers.size})",
                            style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    items(uiState.volunteerOffers) { offer ->
                        VolunteerOfferCard(offer = offer)
                    }
                }
            }
        }
    }
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
                        items(uiState.corroborations) { exp ->
                            ExperienceItem(exp)
                        }
                    }
                }
                StatType.VOLUNTEERS -> {
                    if (uiState.volunteerOffers.isEmpty()) {
                        item { EmptyStatMessage("No volunteer offers yet.") }
                    } else {
                        items(uiState.volunteerOffers) { offer ->
                            VolunteerOfferCard(offer = offer)
                        }
                    }
                }
                StatType.SHARES -> {
                    if (uiState.shares.isEmpty()) {
                        item { EmptyStatMessage("This report hasn't been shared yet.") }
                    } else {
                        items(uiState.shares) { post ->
                            SharedPostItem(post)
                        }
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
                Icon(Icons.Default.AccountCircle, null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
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
                Icon(Icons.Default.AccountCircle, null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(post.authorName, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text(timeAgo(post.createdAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
            Spacer(Modifier.height(4.dp))
            Text(post.title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            Text(post.body, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun EmptyStatMessage(message: String) {
    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(message, color = MaterialTheme.colorScheme.outline)
    }
}
