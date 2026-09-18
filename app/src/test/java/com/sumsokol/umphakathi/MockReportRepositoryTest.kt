package com.sumsokol.umphakathi

import com.sumsokol.umphakathi.data.mock.MockReportRepository
import com.sumsokol.umphakathi.domain.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MockReportRepositoryTest {

    private lateinit var repository: MockReportRepository

    @Before
    fun setUp() {
        repository = MockReportRepository()
    }

    @Test
    fun getReports_returnsInitialSampleReports() = runTest {
        val reports = repository.getReports().first()
        assertTrue(reports.isNotEmpty())
        assertEquals(5, reports.size)
    }

    @Test
    fun getReportById_returnsCorrectReport() = runTest {
        val report = repository.getReport("report-001").first()
        assertNotNull(report)
        assertEquals("Burst water pipe flooding main road", report?.title)
        assertEquals(ReportCategory.WATER_SEWAGE, report?.category)
    }

    @Test
    fun submitReport_addsNewReport() = runTest {
        val newReport = Report(
            id = "",
            reporterId = "user-test",
            title = "Test Incident",
            description = "Test description for unit testing",
            category = ReportCategory.INFRASTRUCTURE,
            urgency = Urgency.MEDIUM,
            potentialHarm = PotentialHarm.MODERATE
        )

        val result = repository.submitReport(newReport)
        assertTrue(result.isSuccess)

        val reports = repository.getReports().first()
        assertEquals(6, reports.size)
        assertTrue(reports.any { it.title == "Test Incident" })
    }

    @Test
    fun addMeToo_incrementsMeTooCount() = runTest {
        val initialCount = repository.getReport("report-001").first()?.meTooCount ?: 0
        val result = repository.addMeToo("report-001", "user-test", "I also saw it")

        assertTrue(result.isSuccess)
        val updatedCount = repository.getReport("report-001").first()?.meTooCount ?: 0
        assertEquals(initialCount + 1, updatedCount)
    }

    @Test
    fun flagAsCrisis_updatesReportStatusToEscalated() = runTest {
        val result = repository.flagAsCrisis("report-003", "user-test")
        assertTrue(result.isSuccess)

        val report = repository.getReport("report-003").first()
        assertEquals(ReportStatus.ESCALATED, report?.status)
    }
}
