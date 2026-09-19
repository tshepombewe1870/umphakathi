package com.sumsokol.umphakathi

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.sumsokol.umphakathi.ui.components.GlobalTopAppBar
import com.sumsokol.umphakathi.ui.community.CommunityScreen
import com.sumsokol.umphakathi.ui.community.CommunityDetailScreen
import com.sumsokol.umphakathi.ui.community.PostDetailScreen
import com.sumsokol.umphakathi.ui.organization.OrganizationScreen
import com.sumsokol.umphakathi.ui.organization.OrganizationDetailScreen
import com.sumsokol.umphakathi.ui.crisis.CrisisDetailScreen
import com.sumsokol.umphakathi.ui.profile.ProfileScreen
import com.sumsokol.umphakathi.ui.profile.PublicProfileScreen
import com.sumsokol.umphakathi.ui.report.CommentBottomSheet
import com.sumsokol.umphakathi.ui.report.ReportsScreen
import com.sumsokol.umphakathi.ui.report.ReportDetailScreen
import com.sumsokol.umphakathi.ui.report.ReportWizardScreen
import com.sumsokol.umphakathi.ui.report.ReportsViewModel
import com.sumsokol.umphakathi.ui.auth.AuthScreen
import com.sumsokol.umphakathi.ui.auth.AuthViewModel
import com.sumsokol.umphakathi.ui.report.ImageSlideshowScreen

@Composable
fun MainNavigation() {
    val authViewModel: AuthViewModel = viewModel()
    val authUiState by authViewModel.uiState.collectAsState()

    if (!authUiState.isAuthenticated) {
        AuthScreen(onAuthenticated = { /* AuthViewModel handles state */ })
        return
    }

    var selectedTab by remember { mutableStateOf<NavKey>(HomeNav) }
    val backStack = rememberNavBackStack(HomeNav)
    var globalSearchQuery by remember { mutableStateOf("") }
    var activeCommentsReportId by remember { mutableStateOf<String?>(null) }
    
    val reportsViewModel: ReportsViewModel = viewModel()
    var activeVolunteerReportId by remember { mutableStateOf<String?>(null) }

    if (activeVolunteerReportId != null) {
        com.sumsokol.umphakathi.ui.report.VolunteerOfferDialog(
            onDismiss = { activeVolunteerReportId = null },
            onSubmit = { types, note ->
                reportsViewModel.submitVolunteerOffer(activeVolunteerReportId ?: "", types, note)
                activeVolunteerReportId = null
            }
        )
    }

    if (activeCommentsReportId != null) {
        val commentsState = reportsViewModel.getComments(activeCommentsReportId ?: "").collectAsState(initial = emptyList())
        CommentBottomSheet(
            reportId = activeCommentsReportId ?: "",
            onDismiss = { activeCommentsReportId = null },
            comments = commentsState.value,
            onAddComment = { body, parentId ->
                reportsViewModel.addComment(activeCommentsReportId ?: "", body, parentId)
            }
        )
    }

    val currentNavKey = backStack.lastOrNull()
    val hideTopBar = currentNavKey is ReportDetailNav || 
                     currentNavKey is AccountNav || 
                     currentNavKey is ReportWizardNav
                     
    val hideBottomBar = currentNavKey is ReportDetailNav || 
                        currentNavKey is ReportWizardNav

    Scaffold(
        topBar = {
            if (!hideTopBar) {
                GlobalTopAppBar(
                    searchQuery = globalSearchQuery,
                    showBackButton = backStack.size > 1,
                    onBack = { backStack.removeLastOrNull() },
                    onAvatarClick = {
                        if (selectedTab !is AccountNav) {
                            selectedTab = AccountNav
                            backStack.add(AccountNav as NavKey)
                        }
                    },
                    onSearch = { globalSearchQuery = it }
                )
            }
        },
        bottomBar = {
            if (!hideBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab is HomeNav,
                        onClick = {
                            selectedTab = HomeNav
                            backStack.add(HomeNav as NavKey)
                        },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = selectedTab is CommunityNav,
                        onClick = {
                            selectedTab = CommunityNav
                            backStack.add(CommunityNav as NavKey)
                        },
                        icon = { Icon(Icons.Default.Groups, contentDescription = "Community") },
                        label = { Text("Community") }
                    )
                    NavigationBarItem(
                        selected = selectedTab is OrganizationNav,
                        onClick = {
                            selectedTab = OrganizationNav
                            backStack.add(OrganizationNav as NavKey)
                        },
                        icon = { Icon(Icons.Default.Business, contentDescription = "Organizations") },
                        label = { Text("Organizations") }
                    )
                    NavigationBarItem(
                        selected = selectedTab is AccountNav,
                        onClick = {
                            selectedTab = AccountNav
                            backStack.add(AccountNav as NavKey)
                        },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Account") },
                        label = { Text("Account") }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryProvider = entryProvider {
                    entry<HomeNav> {
                        ReportsScreen(
                            searchQuery = globalSearchQuery,
                            onReportClick = { id -> 
                                if (id.startsWith("COMM_ID:")) {
                                    backStack.add(CommunityDetailNav(id.removePrefix("COMM_ID:")) as NavKey)
                                } else {
                                    backStack.add(ReportDetailNav(id) as NavKey)
                                }
                            },
                            onCrisisClick = { id -> backStack.add(CrisisDetailNav(id) as NavKey) },
                            onUserClick = { uId -> backStack.add(PublicProfileNav(uId) as NavKey) },
                            onCommentClick = { id -> activeCommentsReportId = id },
                            onVolunteerClick = { id -> activeVolunteerReportId = id },
                            onImageClick = { urls, index -> backStack.add(ImageSlideshowNav(urls, index) as NavKey) },
                            onNewReport = { backStack.add(ReportWizardNav() as NavKey) },
                            viewModel = reportsViewModel
                        )
                    }
                    entry<CommunityNav> {
                        CommunityScreen(
                            searchQuery = globalSearchQuery,
                            onCommunityClick = { id -> backStack.add(CommunityDetailNav(id) as NavKey) },
                            onNewReport = { cId -> backStack.add(ReportWizardNav(cId) as NavKey) }
                        )
                    }
                    entry<OrganizationNav> {
                        OrganizationScreen(
                            searchQuery = globalSearchQuery,
                            onOrganizationClick = { id -> backStack.add(OrganizationDetailNav(id) as NavKey) }
                        )
                    }
                    entry<ReportWizardNav> { key ->
                        ReportWizardScreen(
                            communityId = key.communityId,
                            onBack = { backStack.removeLastOrNull() },
                            onSubmitted = {
                                backStack.removeLastOrNull()
                                selectedTab = HomeNav
                            }
                        )
                    }
                    entry<AccountNav> {
                        ProfileScreen(
                            onNavigateToPublicProfile = { uId -> backStack.add(PublicProfileNav(uId) as NavKey) }
                        )
                    }
                    entry<PublicProfileNav> { key ->
                        PublicProfileScreen(
                            userId = key.userId,
                            searchQuery = globalSearchQuery,
                            onBack = { backStack.removeLastOrNull() },
                            onReportClick = { id -> backStack.add(ReportDetailNav(id) as NavKey) },
                            onUserClick = { uId -> 
                                if (uId != key.userId) backStack.add(PublicProfileNav(uId) as NavKey) 
                            },
                            onCommunityClick = { cId -> backStack.add(CommunityDetailNav(cId) as NavKey) },
                            onCommentClick = { id -> activeCommentsReportId = id },
                            onImageClick = { urls, index -> backStack.add(ImageSlideshowNav(urls, index) as NavKey) }
                        )
                    }
                    entry<ReportDetailNav> { key ->
                        ReportDetailScreen(
                            reportId = key.reportId,
                            onBack = { backStack.removeLastOrNull() },
                            onCrisisClick = { id -> backStack.add(CrisisDetailNav(id) as NavKey) }
                        )
                    }
                    entry<CrisisDetailNav> { key ->
                        CrisisDetailScreen(
                            crisisId = key.crisisId,
                            onBack = { backStack.removeLastOrNull() }
                        )
                    }
                    entry<CommunityDetailNav> { key ->
                        CommunityDetailScreen(
                            communityId = key.communityId,
                            onBack = { backStack.removeLastOrNull() },
                            onPostClick = { id -> backStack.add(ReportDetailNav(id) as NavKey) },
                            onUserClick = { uId -> backStack.add(PublicProfileNav(uId) as NavKey) },
                            onCommentClick = { id -> activeCommentsReportId = id },
                            onImageClick = { urls, index -> backStack.add(ImageSlideshowNav(urls, index) as NavKey) },
                            onNewReport = { cId -> backStack.add(ReportWizardNav(cId) as NavKey) }
                        )
                    }
                    entry<OrganizationDetailNav> { key ->
                        OrganizationDetailScreen(
                            organizationId = key.organizationId,
                            onBack = { backStack.removeLastOrNull() },
                            onReportClick = { id -> backStack.add(ReportDetailNav(id) as NavKey) },
                            onUserClick = { uId -> backStack.add(PublicProfileNav(uId) as NavKey) },
                            onCommunityClick = { cId -> backStack.add(CommunityDetailNav(cId) as NavKey) }
                        )
                    }
                    entry<PostDetailNav> { key ->
                        PostDetailScreen(
                            postId = key.postId,
                            onBack = { backStack.removeLastOrNull() }
                        )
                    }
                    entry<ImageSlideshowNav> { key ->
                        ImageSlideshowScreen(
                            imageUrls = key.imageUrls,
                            initialIndex = key.initialIndex,
                            onBack = { backStack.removeLastOrNull() }
                        )
                    }
                }
            )
        }
    }
}
