package com.sumsokol.umphakathi.domain.model

import java.time.Instant

enum class ReportCategory {
    ABUSE, VIOLENCE, MISSING_PERSON, MEDICAL_EMERGENCY,
    INFRASTRUCTURE, WATER_SEWAGE, FIRE, NATURAL_DISASTER,
    CRIME, UNSAFE_ENVIRONMENT, COMMUNITY_EMERGENCY, OTHER
}

enum class Urgency { LOW, MEDIUM, HIGH, CRITICAL }

enum class PotentialHarm { LOW, MODERATE, HIGH, EXTREME }

enum class ReportStatus {
    SUBMITTED, UNDER_REVIEW, ESCALATED, IN_PROGRESS, RESOLVED, ARCHIVED
}

enum class ContactPermission { NONE, ALLOW_CONTACT }

data class Report(
    val id: String,
    val reporterId: String,
    val reporterName: String = "Anonymous",
    val reporterAvatar: String? = null,
    val crisisId: String? = null,
    val communityId: String? = null,
    val communityName: String? = null,
    val title: String,
    val description: String,
    val category: ReportCategory,
    val urgency: Urgency,
    val potentialHarm: PotentialHarm,
    val status: ReportStatus = ReportStatus.SUBMITTED,
    val incidentLocation: IncidentLocation = IncidentLocation(),
    val locationVisibility: LocationVisibility = LocationVisibility.PUBLIC_APPROXIMATE,
    val incidentStartedAt: Instant? = null,
    val submittedAt: Instant = Instant.now(),
    val peopleAffected: Int? = null,
    val manpowerRequired: Boolean = false,
    val resourcesRequired: List<String> = emptyList(),
    val responsibleOrganizationId: String? = null,
    val contactPermission: ContactPermission = ContactPermission.NONE,
    val hasEvidence: Boolean = false,
    val imageUrls: List<String> = emptyList(),
    val meTooCount: Int = 0,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val volunteerCount: Int = 0,
    val shareCount: Int = 0,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)
