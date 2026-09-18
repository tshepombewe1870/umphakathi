package com.sumsokol.umphakathi.domain.model

import java.time.Instant

enum class ResourceType {
    MANPOWER, TRANSPORT, SUPPLIES, EQUIPMENT, PROFESSIONAL_SKILLS, OTHER
}

enum class VolunteerStatus { OFFERED, ACCEPTED, DECLINED, FULFILLED }

data class VolunteerOffer(
    val id: String,
    val userId: String,
    val userName: String = "Anonymous",
    val reportId: String? = null,
    val crisisId: String? = null,
    val postId: String? = null,
    val resourceType: ResourceType,
    val quantity: Int? = null,
    val note: String? = null,
    val status: VolunteerStatus = VolunteerStatus.OFFERED,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)
