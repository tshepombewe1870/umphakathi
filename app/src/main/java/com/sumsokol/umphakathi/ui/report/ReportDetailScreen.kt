package com.sumsokol.umphakathi.ui.report

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sumsokol.umphakathi.domain.model.ReportStatus
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

    var activeDialog by remember { mutableStateOf<DetailDialog?>(null) }
    var hasMeTood by remember { mutableStateOf(false) }
    var selectedImageIndex by remember { mutableStateOf<Int?>(null) }
    var selectedTab by rememberSaveable { mutableStateOf(DetailTab.OVERVIEW) }
    var showOverflowMenu by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val report = uiState.report
    // NOTE: mirrors the placeholder role check from the previous implementation —
    // swap for a real accountType/role lookup off the user profile when available.
    val currentUserId = com.sumsokol.umphakathi.data.firebase.FirebaseDataModule.currentUserId
    val canPostOfficialUpdate = currentUserId != null
    val canFlagAsCrisis = report != null &&
        report.status != ReportStatus.ESCALATED &&
        report.crisisId == null &&
        report.status != ReportStatus.RESOLVED
    val canResolve = report != null &&
        report.status != ReportStatus.RESOLVED &&
        report.status != ReportStatus.ARCHIVED

    // Full-screen image viewer sits above everything, including dialogs.
    if (report != null && selectedImageIndex != null) {
        ImageSlideshowScreen(
            imageUrls = report.imageUrls,
            initialIndex = selectedImageIndex ?: 0,
            onBack = { selectedImageIndex = null }
        )
        return
    }

    ReportDetailDialogHost(
        activeDialog = activeDialog,
        uiState = uiState,
        onDismiss = { activeDialog = null },
        onConfirmMeToo = { description ->
            viewModel.addMeToo(description)
            hasMeTood = true
            activeDialog = null
        },
        onConfirmResolve = { explanation ->
            viewModel.resolveReport(explanation)
            activeDialog = null
        },
        onSubmitVolunteer = { types, note ->
            viewModel.submitVolunteerOffer(types, note)
            activeDialog = null
        },
        onPostUpdate = { message, status ->
            viewModel.postOfficialUpdate(message, status)
            activeDialog = null
        },
        onAddComment = { body, parentId -> viewModel.addComment(body, parentId) }
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Report Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (report != null) {
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More actions")
                        }
                        DropdownMenu(expanded = showOverflowMenu, onDismissRequest = { showOverflowMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Tag") },
                                leadingIcon = { Icon(Icons.Default.AlternateEmail, null) },
                                onClick = {
                                    showOverflowMenu = false
                                    coroutineScope.launch { snackbarHostState.showSnackbar("Tagging feature coming soon!") }
                                }
                            )
                            if (canPostOfficialUpdate) {
                                DropdownMenuItem(
                                    text = { Text("Post Official Update") },
                                    leadingIcon = { Icon(Icons.Default.Business, null) },
                                    onClick = {
                                        showOverflowMenu = false
                                        activeDialog = DetailDialog.OfficialUpdate
                                    }
                                )
                            }
                            if (canFlagAsCrisis) {
                                DropdownMenuItem(
                                    text = { Text("Flag as Crisis") },
                                    leadingIcon = { Icon(Icons.Default.Warning, null) },
                                    onClick = {
                                        showOverflowMenu = false
                                        viewModel.flagAsCrisis()
                                    }
                                )
                            }
                            if (canResolve) {
                                DropdownMenuItem(
                                    text = { Text("Mark Resolved") },
                                    leadingIcon = { Icon(Icons.Default.CheckCircle, null) },
                                    onClick = {
                                        showOverflowMenu = false
                                        activeDialog = DetailDialog.Resolve
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading || report == null) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                ReportHeaderSection(
                    report = report,
                    onImageClick = { selectedImageIndex = it },
                    onMoreClick = { showOverflowMenu = true }
                )

                PrimaryActionRow(
                    hasMeTood = hasMeTood,
                    onMeToo = { activeDialog = DetailDialog.MeToo },
                    onVolunteer = { activeDialog = DetailDialog.Volunteer },
                    onShare = {
                        viewModel.shareReport()
                        coroutineScope.launch { snackbarHostState.showSnackbar("Report shared to your feed!") }
                    }
                )

                DetailTabRow(selected = selectedTab, onSelect = { selectedTab = it })

                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        DetailTab.OVERVIEW -> OverviewTab(
                            report = report,
                            uiState = uiState,
                            onStatClick = { activeDialog = DetailDialog.Stat(it) }
                        )
                        DetailTab.ACTIVITY -> ActivityTab(
                            officialUpdates = uiState.officialUpdates,
                            auditEvents = uiState.auditEvents
                        )
                        DetailTab.COMMUNITY -> CommunityTab(
                            volunteerOffers = uiState.volunteerOffers,
                            comments = uiState.comments,
                            onLikeOffer = { viewModel.toggleLikeOffer(it) },
                            onCommentOnOffer = {
                                coroutineScope.launch { snackbarHostState.showSnackbar("Volunteer comments coming soon!") }
                            },
                            onOpenComments = { activeDialog = DetailDialog.Comments }
                        )
                    }
                }
            }
        }
    }
}
