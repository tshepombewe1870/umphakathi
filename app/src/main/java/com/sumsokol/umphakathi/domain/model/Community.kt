package com.sumsokol.umphakathi.domain.model

import java.time.Instant

enum class CommunityType { GEOGRAPHIC, ORGANIZATION, OTHER }

data class Community(
    val id: String,
    val name: String,
    val description: String,
    val type: CommunityType,
    val creatorId: String,
    val ownerId: String,
    val moderatorIds: List<String> = emptyList(),
    val location: String? = null,
    val memberCount: Int = 0,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val isActive: Boolean = true
)
