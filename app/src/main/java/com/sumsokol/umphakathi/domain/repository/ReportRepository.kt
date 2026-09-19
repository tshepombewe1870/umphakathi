package com.sumsokol.umphakathi.domain.repository

import com.sumsokol.umphakathi.domain.model.Report
import com.sumsokol.umphakathi.domain.model.ReportStatus
import kotlinx.coroutines.flow.Flow

interface ReportRepository {
    fun getReports(): Flow<List<Report>>
    fun getReport(id: String): Flow<Report?>
    fun getReportsByCrisis(crisisId: String): Flow<List<Report>>
    fun getReportsByStatus(status: ReportStatus): Flow<List<Report>>
    fun getReportsForCommunity(communityId: String): Flow<List<Report>>
    suspend fun submitReport(report: Report): Result<Report>
    suspend fun updateReport(report: Report): Result<Report>
    suspend fun addMeToo(reportId: String, userId: String, description: String?): Result<Unit>
    suspend fun toggleLikeReport(reportId: String, userId: String): Result<Unit>
    suspend fun toggleLikeComment(commentId: String, userId: String): Result<Unit>
    suspend fun flagAsCrisis(reportId: String, userId: String): Result<Unit>
    suspend fun resolveReport(reportId: String, resolution: com.sumsokol.umphakathi.domain.model.Resolution): Result<Unit>
    fun getComments(reportId: String): Flow<List<com.sumsokol.umphakathi.domain.model.Comment>>
    suspend fun addComment(comment: com.sumsokol.umphakathi.domain.model.Comment): Result<com.sumsokol.umphakathi.domain.model.Comment>
    fun getOfficialUpdates(reportId: String): Flow<List<com.sumsokol.umphakathi.domain.model.OfficialUpdate>>
    suspend fun addOfficialUpdate(update: com.sumsokol.umphakathi.domain.model.OfficialUpdate): Result<com.sumsokol.umphakathi.domain.model.OfficialUpdate>
    fun getAuditEvents(reportId: String): Flow<List<com.sumsokol.umphakathi.domain.model.AuditEvent>>
    fun getExperiences(reportId: String): Flow<List<com.sumsokol.umphakathi.domain.model.ReportExperience>>
    suspend fun shareReport(reportId: String, userId: String): Result<Unit>
}
