package com.sumsokol.umphakathi.domain.model

import java.time.Instant

enum class AuditAction {
    REPORT_CREATED,
    REPORT_UPDATED,
    CRISIS_FLAGGED,
    CRISIS_CREATED,
    ESCALATED,
    DEPARTMENT_RESPONDED,
    VOLUNTEER_OFFERED,
    COMMENT_ADDED,
    OFFICIAL_UPDATE_ADDED,
    RESOLUTION_SUBMITTED,
    REPORT_RESOLVED,
    REPORT_ARCHIVED
}

data class AuditEvent(
    val id: String,
    val entityType: String,
    val entityId: String,
    val actorId: String,
    val action: AuditAction,
    val metadata: Map<String, String> = emptyMap(),
    val createdAt: Instant = Instant.now()
)
