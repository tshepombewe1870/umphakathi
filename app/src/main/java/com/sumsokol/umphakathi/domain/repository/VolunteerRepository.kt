package com.sumsokol.umphakathi.domain.repository

import com.sumsokol.umphakathi.domain.model.VolunteerOffer
import com.sumsokol.umphakathi.domain.model.VolunteerStatus
import kotlinx.coroutines.flow.Flow

interface VolunteerRepository {
    fun getOffersForReport(reportId: String): Flow<List<VolunteerOffer>>
    fun getOffersForCrisis(crisisId: String): Flow<List<VolunteerOffer>>
    fun getOffersByUser(userId: String): Flow<List<VolunteerOffer>>
    suspend fun submitOffer(offer: VolunteerOffer): Result<VolunteerOffer>
    suspend fun updateOfferStatus(offerId: String, status: VolunteerStatus): Result<Unit>
    suspend fun toggleLikeOffer(offerId: String, userId: String): Result<Unit>
    fun getComments(offerId: String): Flow<List<com.sumsokol.umphakathi.domain.model.Comment>>
    suspend fun addComment(comment: com.sumsokol.umphakathi.domain.model.Comment): Result<com.sumsokol.umphakathi.domain.model.Comment>
}
