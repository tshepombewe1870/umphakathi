package com.sumsokol.umphakathi.domain.model

import java.time.Instant

enum class CrisisSignalType {
    COMMUNITY_FLAG,
    ABUSE_REPORT,
    VIOLENCE_REPORT,
    FIRE_REPORT,
    NATURAL_DISASTER_REPORT,
    MULTIPLE_REPORTS,
    HIGH_POTENTIAL_HARM,
    LONG_DURATION,
    COMMUNITY_ESCALATION
}

data class CrisisSignal(
    val id: String,
    val crisisId: String,
    val userId: String,
    val type: CrisisSignalType,
    val createdAt: Instant = Instant.now()
)
