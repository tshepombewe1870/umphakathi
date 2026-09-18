package com.sumsokol.umphakathi.domain.model

import java.time.Instant

enum class AccountType { PERSON, ORGANIZATION }

data class User(
    val id: String,
    val username: String,
    val accountType: AccountType = AccountType.PERSON,
    val role: String? = null,
    val communityName: String? = null,
    val verified: Boolean = false,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val isActive: Boolean = true
)

data class PrivateUserInfo(
    val userId: String,
    val phone: String? = null,
    val email: String? = null,
    val contactAllowed: Boolean = false,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)
