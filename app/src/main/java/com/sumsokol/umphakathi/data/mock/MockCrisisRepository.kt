package com.sumsokol.umphakathi.data.mock

import com.sumsokol.umphakathi.domain.model.*
import com.sumsokol.umphakathi.domain.repository.CrisisRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class MockCrisisRepository : CrisisRepository {
    private val _crises = MutableStateFlow(sampleCrises())

    override fun getCrises(): Flow<List<Crisis>> = _crises

    override fun getCrisis(id: String): Flow<Crisis?> =
        _crises.map { list -> list.find { it.id == id } }

    override suspend fun createCrisis(crisis: Crisis): Result<Crisis> {
        val newCrisis = crisis.copy(id = UUID.randomUUID().toString(), createdAt = Instant.now())
        _crises.value = _crises.value + newCrisis
        return Result.success(newCrisis)
    }

    override suspend fun updateCrisis(crisis: Crisis): Result<Crisis> {
        _crises.value = _crises.value.map { if (it.id == crisis.id) crisis else it }
        return Result.success(crisis)
    }

    override suspend fun addVolunteerOffer(crisisId: String, offer: VolunteerOffer): Result<Unit> =
        Result.success(Unit)

    private fun sampleCrises() = listOf(
        Crisis(
            id = "crisis-001",
            title = "Community Water Crisis - Soweto",
            description = "Multiple burst pipes and water outages affecting thousands of residents across Soweto. Coordinated response required.",
            category = ReportCategory.WATER_SEWAGE,
            status = CrisisStatus.RESPONSE_IN_PROGRESS,
            severity = Urgency.HIGH,
            urgency = Urgency.HIGH,
            potentialHarm = PotentialHarm.HIGH,
            incidentLocation = IncidentLocation(
                source = LocationSource.MANUALLY_DESCRIBED,
                city = "Johannesburg",
                neighborhood = "Soweto"
            ),
            reportCount = 14,
            independentReporterCount = 11,
            meTooCount = 47,
            peopleAffected = 3500,
            manpowerRequired = true,
            detectedAt = Instant.now().minus(20, ChronoUnit.HOURS),
            createdAt = Instant.now().minus(18, ChronoUnit.HOURS),
            updatedAt = Instant.now().minus(1, ChronoUnit.HOURS)
        ),
        Crisis(
            id = "crisis-002",
            title = "Missing Teenager Alert - Alexandra",
            description = "14-year-old female reported missing. Community search effort being coordinated.",
            category = ReportCategory.MISSING_PERSON,
            status = CrisisStatus.ACTIVE,
            severity = Urgency.CRITICAL,
            urgency = Urgency.CRITICAL,
            potentialHarm = PotentialHarm.EXTREME,
            incidentLocation = IncidentLocation(
                source = LocationSource.MANUALLY_DESCRIBED,
                city = "Johannesburg",
                neighborhood = "Alexandra"
            ),
            reportCount = 1,
            independentReporterCount = 1,
            meTooCount = 0,
            manpowerRequired = true,
            detectedAt = Instant.now().minus(25, ChronoUnit.HOURS),
            createdAt = Instant.now().minus(24, ChronoUnit.HOURS),
            updatedAt = Instant.now().minus(6, ChronoUnit.HOURS)
        )
    )
}
