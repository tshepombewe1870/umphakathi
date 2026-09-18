package com.sumsokol.umphakathi.domain.model

import java.time.Instant

enum class NoticeType {
    PLANNED_OUTAGE,
    COMMUNITY_EVENT,
    PROTEST_OR_MARCH
}

enum class NoticeStatus {
    PENDING_APPROVAL,
    APPROVED,
    REJECTED
}

data class Notice(
    val id: String,
    val communityId: String,
    val creatorId: String,
    val creatorName: String,
    val creatorType: String, // "ORGANIZATION" or "PERSON"
    val title: String,
    val body: String,
    val type: NoticeType,
    val status: NoticeStatus = NoticeStatus.PENDING_APPROVAL,
    val startTime: Instant? = null,
    val endTime: Instant? = null,
    val locationDescription: String? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)
