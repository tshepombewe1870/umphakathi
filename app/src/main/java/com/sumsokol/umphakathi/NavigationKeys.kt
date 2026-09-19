package com.sumsokol.umphakathi

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface AppNavKey : NavKey

@Serializable data object AuthNav : AppNavKey
@Serializable data object HomeNav : AppNavKey
@Serializable data object CommunityNav : AppNavKey
@Serializable data object OrganizationNav : AppNavKey
@Serializable data object AccountNav : AppNavKey

@Serializable data class ReportDetailNav(val reportId: String) : AppNavKey
@Serializable data class CrisisDetailNav(val crisisId: String) : AppNavKey
@Serializable data class CommunityDetailNav(val communityId: String) : AppNavKey
@Serializable data class PostDetailNav(val postId: String) : AppNavKey
@Serializable data class OrganizationDetailNav(val organizationId: String) : AppNavKey
@Serializable data class ReportWizardNav(val communityId: String? = null) : AppNavKey
@Serializable data class ImageSlideshowNav(val imageUrls: List<String>, val initialIndex: Int = 0) : AppNavKey
@Serializable data class PublicProfileNav(val userId: String) : AppNavKey
