package com.sumsokol.umphakathi.data.mock

import com.sumsokol.umphakathi.domain.model.*
import com.sumsokol.umphakathi.domain.repository.VolunteerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class MockVolunteerRepository : VolunteerRepository {
    private val _offers = MutableStateFlow(sampleOffers())

    override fun getOffersForReport(reportId: String): Flow<List<VolunteerOffer>> =
        _offers.map { list -> list.filter { it.reportId == reportId } }

    override fun getOffersForCrisis(crisisId: String): Flow<List<VolunteerOffer>> =
        _offers.map { list -> list.filter { it.crisisId == crisisId } }

    override fun getOffersByUser(userId: String): Flow<List<VolunteerOffer>> =
        _offers.map { list -> list.filter { it.userId == userId } }

    override suspend fun submitOffer(offer: VolunteerOffer): Result<VolunteerOffer> {
        val newOffer = offer.copy(id = UUID.randomUUID().toString(), createdAt = Instant.now())
        _offers.value = _offers.value + newOffer
        return Result.success(newOffer)
    }

    override suspend fun updateOfferStatus(offerId: String, status: VolunteerStatus): Result<Unit> {
        _offers.value = _offers.value.map {
            if (it.id == offerId) it.copy(status = status) else it
        }
        return Result.success(Unit)
    }

    override suspend fun toggleLikeOffer(offerId: String, userId: String): Result<Unit> {
        _offers.value = _offers.value.map {
            if (it.id == offerId) it.copy(likeCount = it.likeCount + 1) else it
        }
        return Result.success(Unit)
    }

    override fun getComments(offerId: String): Flow<List<Comment>> = 
        kotlinx.coroutines.flow.flowOf(emptyList()) // Simple mock

    override suspend fun addComment(comment: Comment): Result<Comment> {
        return Result.success(comment)
    }

    private fun sampleOffers() = listOf(
        VolunteerOffer(
            id = "offer-001",
            userId = "user-aaa",
            userName = "Thabo N.",
            reportId = "report-001",
            crisisId = "crisis-001",
            postId = "post-002",
            resourceType = ResourceType.TRANSPORT,
            quantity = 1,
            note = "Have a bakkie, can help transport water containers",
            status = VolunteerStatus.ACCEPTED,
            likeCount = 12,
            commentCount = 3,
            createdAt = Instant.now().minus(5, ChronoUnit.HOURS)
        ),
        VolunteerOffer(
            id = "offer-002",
            userId = "user-bbb",
            userName = "Lerato M.",
            reportId = "report-001",
            crisisId = "crisis-001",
            resourceType = ResourceType.MANPOWER,
            quantity = 3,
            note = "Myself and 2 family members available to help",
            status = VolunteerStatus.OFFERED,
            likeCount = 5,
            createdAt = Instant.now().minus(4, ChronoUnit.HOURS)
        ),
        VolunteerOffer(
            id = "offer-003",
            userId = "user-ccc",
            userName = "David K.",
            reportId = "report-002",
            crisisId = "crisis-002",
            postId = "post-003",
            resourceType = ResourceType.MANPOWER,
            quantity = 5,
            note = "Search party ready with flashlights and water for the team",
            status = VolunteerStatus.ACCEPTED,
            likeCount = 24,
            commentCount = 8,
            createdAt = Instant.now().minus(18, ChronoUnit.HOURS)
        ),
        VolunteerOffer(
            id = "offer-004",
            userId = "user-ddd",
            userName = "Sarah J.",
            reportId = "report-001",
            resourceType = ResourceType.SUPPLIES,
            quantity = 20,
            note = "Can donate 20L bottles of water",
            status = VolunteerStatus.OFFERED,
            likeCount = 2,
            createdAt = Instant.now().minus(2, ChronoUnit.HOURS)
        )
    )
}
