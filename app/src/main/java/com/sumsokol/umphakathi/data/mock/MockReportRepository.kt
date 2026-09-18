package com.sumsokol.umphakathi.data.mock

import com.sumsokol.umphakathi.domain.model.*
import com.sumsokol.umphakathi.domain.repository.ReportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class MockReportRepository : ReportRepository {
    private val _reports = MutableStateFlow(sampleReports())
    private val _comments = MutableStateFlow(sampleComments())

    override fun getReports(): Flow<List<Report>> = _reports

    override fun getReport(id: String): Flow<Report?> =
        _reports.map { list -> list.find { it.id == id } }

    override fun getReportsByCrisis(crisisId: String): Flow<List<Report>> =
        _reports.map { list -> list.filter { it.crisisId == crisisId } }

    override fun getReportsByStatus(status: ReportStatus): Flow<List<Report>> =
        _reports.map { list -> list.filter { it.status == status } }

    override suspend fun submitReport(report: Report): Result<Report> {
        val newReport = report.copy(id = UUID.randomUUID().toString(), submittedAt = Instant.now())
        _reports.value = _reports.value + newReport
        return Result.success(newReport)
    }

    override suspend fun updateReport(report: Report): Result<Report> {
        _reports.value = _reports.value.map { if (it.id == report.id) report else it }
        return Result.success(report)
    }

    override suspend fun addMeToo(reportId: String, userId: String, description: String?): Result<Unit> {
        _reports.value = _reports.value.map {
            if (it.id == reportId) it.copy(meTooCount = it.meTooCount + 1) else it
        }
        
        // If description is provided, essentially treat it as a comment as well
        if (!description.isNullOrBlank()) {
            addComment(Comment(
                id = UUID.randomUUID().toString(),
                postId = reportId, // Reusing postId for reportId in comments
                authorId = userId,
                body = "Corroborated: $description",
                createdAt = Instant.now()
            ))
        }
        
        return Result.success(Unit)
    }

    override suspend fun toggleLikeReport(reportId: String, userId: String): Result<Unit> {
        _reports.value = _reports.value.map {
            if (it.id == reportId) it.copy(likeCount = it.likeCount + 1) else it
        }
        return Result.success(Unit)
    }

    override suspend fun toggleLikeComment(commentId: String, userId: String): Result<Unit> {
        _comments.value = _comments.value.map {
            if (it.id == commentId) it.copy(likeCount = it.likeCount + 1) else it
        }
        return Result.success(Unit)
    }

    override fun getComments(reportId: String): Flow<List<Comment>> = 
        _comments.map { list -> list.filter { it.postId == reportId } }

    override suspend fun addComment(comment: Comment): Result<Comment> {
        val newComment = comment.copy(id = UUID.randomUUID().toString(), createdAt = Instant.now())
        _comments.value = _comments.value + newComment
        _reports.value = _reports.value.map {
            if (it.id == comment.postId) it.copy(commentCount = it.commentCount + 1) else it
        }
        return Result.success(newComment)
    }

    override suspend fun flagAsCrisis(reportId: String, userId: String): Result<Unit> {
        _reports.value = _reports.value.map {
            if (it.id == reportId) it.copy(status = ReportStatus.ESCALATED) else it
        }
        return Result.success(Unit)
    }

    override suspend fun resolveReport(reportId: String, resolution: Resolution): Result<Unit> {
        _reports.value = _reports.value.map {
            if (it.id == reportId) it.copy(status = ReportStatus.RESOLVED, updatedAt = Instant.now()) else it
        }
        return Result.success(Unit)
    }

    override suspend fun shareReport(reportId: String, userId: String): Result<Unit> {
        _reports.value = _reports.value.map {
            if (it.id == reportId) it.copy(shareCount = it.shareCount + 1) else it
        }
        return Result.success(Unit)
    }

    override fun getOfficialUpdates(reportId: String): Flow<List<OfficialUpdate>> = kotlinx.coroutines.flow.flowOf(
        if (reportId == "report-001") listOf(
            OfficialUpdate(
                id = "update-1",
                reportId = "report-001",
                organizationId = "org-1",
                organizationName = "Department of Water",
                message = "Maintenance team dispatched to the location. Estimated time of arrival is 2 hours.",
                statusUpdate = ReportStatus.IN_PROGRESS,
                createdAt = Instant.now().minus(4, java.time.temporal.ChronoUnit.HOURS)
            ),
            OfficialUpdate(
                id = "update-2",
                reportId = "report-001",
                organizationId = "org-1",
                organizationName = "Department of Water",
                message = "Team has arrived and identified the burst pipe. Work is underway to isolate the leak.",
                createdAt = Instant.now().minus(2, java.time.temporal.ChronoUnit.HOURS)
            )
        ) else emptyList()
    )

    override suspend fun addOfficialUpdate(update: OfficialUpdate): Result<OfficialUpdate> {
        val newUpdate = update.copy(id = UUID.randomUUID().toString(), createdAt = Instant.now())
        // In a real mock we might store these in a flow, but for now we'll just return success
        return Result.success(newUpdate)
    }

    override fun getAuditEvents(reportId: String): Flow<List<AuditEvent>> = kotlinx.coroutines.flow.flowOf(
        listOf(
            AuditEvent(
                id = "audit-1",
                entityType = "REPORT",
                entityId = reportId,
                actorId = "user-123",
                action = AuditAction.REPORT_CREATED,
                createdAt = Instant.now().minus(24, java.time.temporal.ChronoUnit.HOURS)
            ),
            AuditEvent(
                id = "audit-2",
                entityType = "REPORT",
                entityId = reportId,
                actorId = "system",
                action = AuditAction.REPORT_UPDATED,
                metadata = mapOf("status" to "UNDER_REVIEW"),
                createdAt = Instant.now().minus(20, java.time.temporal.ChronoUnit.HOURS)
            )
        )
    )

    override fun getExperiences(reportId: String): Flow<List<ReportExperience>> = kotlinx.coroutines.flow.flowOf(
        listOf(
            ReportExperience(
                id = "exp-1",
                reportId = reportId,
                userId = "user-789",
                userName = "Sipho Dlamini",
                description = "I saw this too on my way to work.",
                createdAt = Instant.now().minus(10, java.time.temporal.ChronoUnit.HOURS)
            ),
            ReportExperience(
                id = "exp-2",
                reportId = reportId,
                userId = "user-101",
                userName = "Zanele Mthembu",
                description = "Water is starting to enter my yard as well. It's getting serious.",
                createdAt = Instant.now().minus(8, java.time.temporal.ChronoUnit.HOURS)
            ),
            ReportExperience(
                id = "exp-3",
                reportId = reportId,
                userId = "user-202",
                userName = "Kevin Naidoo",
                description = "Tried to drive past, road is definitely blocked. Use the back route instead.",
                createdAt = Instant.now().minus(5, java.time.temporal.ChronoUnit.HOURS)
            )
        )
    )

    private fun sampleReports() = listOf(
        Report(
            id = "report-001",
            reporterId = "user-123",
            crisisId = "crisis-001",
            communityId = "community-001",
            title = "Burst water pipe flooding main road",
            description = "A burst municipal water pipe has been flooding the main street since last night. Several homes are now inaccessible and the road is completely blocked.",
            category = ReportCategory.WATER_SEWAGE,
            urgency = Urgency.HIGH,
            potentialHarm = PotentialHarm.HIGH,
            status = ReportStatus.IN_PROGRESS,
            incidentLocation = IncidentLocation(
                source = LocationSource.PHONE_LOCATION,
                latitude = -26.2041,
                longitude = 28.0473,
                city = "Johannesburg",
                neighborhood = "Soweto"
            ),
            locationVisibility = LocationVisibility.PUBLIC_APPROXIMATE,
            incidentStartedAt = Instant.now().minus(18, ChronoUnit.HOURS),
            submittedAt = Instant.now().minus(16, ChronoUnit.HOURS),
            peopleAffected = 120,
            meTooCount = 14,
            commentCount = 3, // Updated to match actual count
            volunteerCount = 5,
            createdAt = Instant.now().minus(16, ChronoUnit.HOURS),
            updatedAt = Instant.now().minus(2, ChronoUnit.HOURS)
        ),
        Report(
            id = "report-002",
            reporterId = "user-456",
            communityId = "community-001",
            title = "Domestic violence situation - neighbour needs help",
            description = "Repeatedly hearing domestic altercations from the house next door. Sounds like children are also involved.",
            category = ReportCategory.ABUSE,
            urgency = Urgency.CRITICAL,
            potentialHarm = PotentialHarm.EXTREME,
            status = ReportStatus.ESCALATED,
            incidentLocation = IncidentLocation(
                source = LocationSource.MANUALLY_DESCRIBED,
                addressDescription = "Corner of Klipspruit Valley Rd",
                city = "Johannesburg",
                neighborhood = "Soweto"
            ),
            locationVisibility = LocationVisibility.RESPONDERS_ONLY,
            incidentStartedAt = Instant.now().minus(3, ChronoUnit.HOURS),
            submittedAt = Instant.now().minus(2, ChronoUnit.HOURS),
            meTooCount = 3,
            commentCount = 1, // Updated to match actual count
            volunteerCount = 12,
            createdAt = Instant.now().minus(2, ChronoUnit.HOURS),
            updatedAt = Instant.now().minus(30, ChronoUnit.MINUTES)
        ),
        Report(
            id = "report-003",
            reporterId = "user-789",
            communityId = "community-002",
            title = "Pothole causing vehicle damage on N12",
            description = "Large pothole on the N12 near Crown Mines interchange. Multiple vehicles have had tyre blowouts. Dangerous at night.",
            category = ReportCategory.INFRASTRUCTURE,
            urgency = Urgency.MEDIUM,
            potentialHarm = PotentialHarm.MODERATE,
            status = ReportStatus.SUBMITTED,
            incidentLocation = IncidentLocation(
                source = LocationSource.MANUALLY_SELECTED,
                latitude = -26.2350,
                longitude = 27.9700,
                city = "Johannesburg",
                neighborhood = "Crown Mines"
            ),
            locationVisibility = LocationVisibility.PUBLIC_EXACT,
            incidentStartedAt = Instant.now().minus(7, ChronoUnit.DAYS),
            submittedAt = Instant.now().minus(5, ChronoUnit.DAYS),
            meTooCount = 27,
            commentCount = 0,
            createdAt = Instant.now().minus(5, ChronoUnit.DAYS),
            updatedAt = Instant.now().minus(5, ChronoUnit.DAYS)
        ),
        Report(
            id = "report-004",
            reporterId = "user-321",
            crisisId = "crisis-002",
            communityId = "community-003",
            title = "Missing teenager - Alexandra Township",
            description = "14-year-old girl missing since yesterday morning. Last seen near the taxi rank. Family is very worried.",
            category = ReportCategory.MISSING_PERSON,
            urgency = Urgency.CRITICAL,
            potentialHarm = PotentialHarm.EXTREME,
            status = ReportStatus.UNDER_REVIEW,
            incidentLocation = IncidentLocation(
                source = LocationSource.MANUALLY_DESCRIBED,
                addressDescription = "Alexandra Township taxi rank area",
                city = "Johannesburg",
                neighborhood = "Alexandra"
            ),
            locationVisibility = LocationVisibility.RESPONDERS_ONLY,
            incidentStartedAt = Instant.now().minus(26, ChronoUnit.HOURS),
            submittedAt = Instant.now().minus(24, ChronoUnit.HOURS),
            meTooCount = 1,
            commentCount = 0,
            createdAt = Instant.now().minus(24, ChronoUnit.HOURS),
            updatedAt = Instant.now().minus(12, ChronoUnit.HOURS)
        ),
        Report(
            id = "report-005",
            reporterId = "user-654",
            communityId = "community-001",
            title = "Illegal dumping blocking stormwater drain",
            description = "Residents have been illegally dumping refuse near the stormwater drain. After last night's rain the entire intersection flooded.",
            category = ReportCategory.UNSAFE_ENVIRONMENT,
            urgency = Urgency.MEDIUM,
            potentialHarm = PotentialHarm.MODERATE,
            status = ReportStatus.SUBMITTED,
            incidentLocation = IncidentLocation(
                source = LocationSource.PHONE_LOCATION,
                latitude = -26.2200,
                longitude = 28.0100,
                city = "Johannesburg",
                neighborhood = "Soweto"
            ),
            locationVisibility = LocationVisibility.PUBLIC_APPROXIMATE,
            incidentStartedAt = Instant.now().minus(2, ChronoUnit.DAYS),
            submittedAt = Instant.now().minus(1, ChronoUnit.DAYS),
            meTooCount = 8,
            commentCount = 0,
            createdAt = Instant.now().minus(1, ChronoUnit.DAYS),
            updatedAt = Instant.now().minus(1, ChronoUnit.DAYS)
        )
    )

    private fun sampleComments() = listOf(
        Comment(
            id = "comment-r1-1",
            postId = "report-001",
            authorId = "user-789",
            body = "I'm also in the area, the water level is rising fast!",
            createdAt = Instant.now().minus(15, ChronoUnit.HOURS)
        ),
        Comment(
            id = "comment-r1-2",
            postId = "report-001",
            authorId = "user-123",
            parentCommentId = "comment-r1-1",
            body = "The municipal team is here now, they just arrived.",
            createdAt = Instant.now().minus(14, ChronoUnit.HOURS)
        ),
        Comment(
            id = "comment-r1-3",
            postId = "report-001",
            authorId = "user-456",
            body = "Does anyone know if power will be affected?",
            createdAt = Instant.now().minus(12, ChronoUnit.HOURS)
        ),
        Comment(
            id = "comment-r2-1",
            postId = "report-002",
            authorId = "user-123",
            body = "I've called the local security cluster to check in.",
            createdAt = Instant.now().minus(1, ChronoUnit.HOURS)
        )
    )
}
