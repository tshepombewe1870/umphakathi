package com.sumsokol.umphakathi.domain.model

import java.time.Instant

enum class PostStatus { ACTIVE, RESOLVED, ARCHIVED }

data class CommunityPost(
    val id: String,
    val communityId: String,
    val authorId: String,
    val authorName: String = "Anonymous",
    val communityName: String? = null,
    val isAnonymous: Boolean = false,
    val reportId: String? = null,
    val crisisId: String? = null,
    val title: String,
    val body: String,
    val category: ReportCategory = ReportCategory.OTHER,
    val urgency: Urgency = Urgency.MEDIUM,
    val status: PostStatus = PostStatus.ACTIVE,
    val commentCount: Int = 0,
    val meTooCount: Int = 0,
    val volunteerCount: Int = 0,
    val imageUrls: List<String> = emptyList(),
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)
