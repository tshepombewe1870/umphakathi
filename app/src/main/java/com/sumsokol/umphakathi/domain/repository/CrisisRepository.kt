package com.sumsokol.umphakathi.domain.repository

import com.sumsokol.umphakathi.domain.model.Crisis
import com.sumsokol.umphakathi.domain.model.VolunteerOffer
import kotlinx.coroutines.flow.Flow

interface CrisisRepository {
    fun getCrises(): Flow<List<Crisis>>
    fun getCrisis(id: String): Flow<Crisis?>
    suspend fun createCrisis(crisis: Crisis): Result<Crisis>
    suspend fun updateCrisis(crisis: Crisis): Result<Crisis>
    suspend fun addVolunteerOffer(crisisId: String, offer: VolunteerOffer): Result<Unit>
}
