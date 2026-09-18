package com.sumsokol.umphakathi.domain.model

import java.time.Instant

enum class EscalationStatus { PENDING, ACKNOWLEDGED, RESOLVED }

data class Escalation(
    val id: String,
    val reportId: String? = null,
    val crisisId: String? = null,
    val postId: String? = null,
    val initiatedBy: String,
    val targetOrganizationId: String,
    val reason: String,
    val status: EscalationStatus = EscalationStatus.PENDING,
    val createdAt: Instant = Instant.now(),
    val resolvedAt: Instant? = null
)
