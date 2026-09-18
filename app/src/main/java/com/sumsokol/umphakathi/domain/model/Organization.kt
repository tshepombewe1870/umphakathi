package com.sumsokol.umphakathi.domain.model

import java.time.Instant

enum class OrganizationType {
    GOVERNMENT_DEPARTMENT,
    NGO,
    CHARITY,
    EMERGENCY_SERVICES,
    COMMUNITY_ORGANIZATION,
    OTHER
}

data class Organization(
    val id: String,
    val accountUserId: String,
    val name: String,
    val organizationType: OrganizationType,
    val verified: Boolean = false,
    val description: String? = null,
    val serviceAreas: List<String> = emptyList(),
    val createdAt: Instant = Instant.now()
)
