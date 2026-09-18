package com.sumsokol.umphakathi.data.firebase

import android.content.Context
import com.sumsokol.umphakathi.data.local.SessionManager
import com.sumsokol.umphakathi.domain.repository.CommunityRepository
import com.sumsokol.umphakathi.domain.repository.CrisisRepository
import com.sumsokol.umphakathi.domain.repository.ReportRepository
import com.sumsokol.umphakathi.domain.repository.VolunteerRepository
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

object FirebaseDataModule {
    val firestore = Firebase.firestore
    val storage = Firebase.storage
    
    private var sessionManager: SessionManager? = null

    fun init(context: Context) {
        sessionManager = SessionManager.getInstance(context)
    }

    val userIdFlow: kotlinx.coroutines.flow.StateFlow<String?>
        get() = sessionManager?.userIdFlow ?: kotlinx.coroutines.flow.MutableStateFlow(null)

    val currentUserId: String?
        get() = sessionManager?.getUserId()

    val reportRepository: ReportRepository by lazy { FirestoreReportRepository(firestore) }
    val crisisRepository: CrisisRepository by lazy { FirestoreCrisisRepository(firestore) }
    val communityRepository: CommunityRepository by lazy { FirestoreCommunityRepository(firestore) }
    val volunteerRepository: VolunteerRepository by lazy { FirestoreVolunteerRepository(firestore) }
    val userRepository: FirestoreUserRepository by lazy { FirestoreUserRepository(firestore) }
}
