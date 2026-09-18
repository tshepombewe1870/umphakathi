package com.sumsokol.umphakathi.data.firebase

import com.sumsokol.umphakathi.domain.model.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.temporal.ChronoUnit

object FirebaseSeeder {
    suspend fun seedIfEmpty(db: FirebaseFirestore) {
        val snapshot = db.collection("reports").limit(1).get().await()
        if (snapshot.isEmpty) {
            seed(db)
        }
    }

    suspend fun seed(db: FirebaseFirestore) {
        val batch = db.batch()

        // --- CONSTANTS FOR CONSISTENCY ---
        // Users
        val USER_THABO = "user-thabo"
        val USER_LERATO = "user-lerato"
        val USER_SIPHO = "user-sipho"
        val USER_NALEDI = "user-naledi"
        val USER_PRIYA = "user-priya"
        val USER_JABU = "user-jabu"
        val USER_NOMSA = "user-nomsa"
        val USER_KHAYA = "user-khaya"
        val USER_ZANELE = "user-zanele"
        val USER_BONGANI = "user-bongani"
        val USER_TSHEPO = "user-tshepo"
        val USER_KELE = "user-kele"
        val USER_ANDISIWE = "user-andisiwe"
        val USER_MANDLA = "user-mandla"
        val USER_RENEILWE = "user-reneilwe"
        
        // Organizations
        val USER_WATER_DEPT = "user-water-dept"
        val USER_SAPS = "user-saps"
        val ORG_WATER = "org-water"
        val ORG_SAPS = "org-saps"

        // Communities
        val COMM_SOWETO = "comm-soweto"
        val COMM_ALEX = "comm-alex"
        val COMM_NGO = "comm-ngo"

        // Reports & Crises
        val RPT_BURST_PIPE = "rpt-burst-pipe"
        val RPT_POWER_OUTAGE = "rpt-power-rank"
        val CRS_WATER_CRISIS = "crs-soweto-water"

        // 1. USERS (15 real users + 2 org-linked users)
        val users = listOf(
            User(USER_THABO, "Thabo Nkosi", AccountType.PERSON, "Resident", "Soweto West", true),
            User(USER_LERATO, "Lerato Mokoena", AccountType.PERSON, "Moderator", "Soweto West", true),
            User(USER_SIPHO, "Sipho Dlamini", AccountType.PERSON, "Resident", "Soweto West", false),
            User(USER_NALEDI, "Naledi Khumalo", AccountType.PERSON, "Resident", "Alexandra Township", true),
            User(USER_PRIYA, "Dr. Priya Naidoo", AccountType.PERSON, "Researcher", "Joburg Monitoring NGO", true),
            User(USER_JABU, "Jabu Cele", AccountType.PERSON, "Resident", "Soweto West", true),
            User(USER_NOMSA, "Nomsa Buthelezi", AccountType.PERSON, "Resident", "Soweto West", true),
            User(USER_KHAYA, "Khaya Zondo", AccountType.PERSON, "Resident", "Soweto West", false),
            User(USER_ZANELE, "Zanele Sithole", AccountType.PERSON, "Resident", "Soweto West", true),
            User(USER_BONGANI, "Bongani Mavuso", AccountType.PERSON, "Resident", "Soweto West", true),
            User(USER_TSHEPO, "Tshepo Mbeki", AccountType.PERSON, "Resident", "Soweto West", true),
            User(USER_KELE, "Kelebogile Modise", AccountType.PERSON, "Resident", "Soweto West", true),
            User(USER_ANDISIWE, "Andisiwe Gwala", AccountType.PERSON, "Resident", "Alexandra Township", true),
            User(USER_MANDLA, "Mandla Khoza", AccountType.PERSON, "Resident", "Alexandra Township", true),
            User(USER_RENEILWE, "Reneilwe Malatji", AccountType.PERSON, "Resident", "Alexandra Township", true),
            User(USER_WATER_DEPT, "Joburg Water Official", AccountType.ORGANIZATION, "Official", "Department of Water", true),
            User(USER_SAPS, "SAPS Alexandra Duty Officer", AccountType.ORGANIZATION, "Responder", "SAPS", true)
        )
        users.forEach { batch.set(db.collection("users").document(it.id), it.toFirestoreMap()) }

        // 2. ORGANIZATIONS
        val orgs = listOf(
            Organization(ORG_WATER, USER_WATER_DEPT, "Joburg Water", OrganizationType.GOVERNMENT_DEPARTMENT, true, "City of Johannesburg Water Department"),
            Organization(ORG_SAPS, USER_SAPS, "SAPS Alexandra", OrganizationType.EMERGENCY_SERVICES, true, "South African Police Service")
        )
        orgs.forEach { batch.set(db.collection("organizations").document(it.id), it.toFirestoreMap()) }

        // 3. COMMUNITIES
        // Soweto has 10 members, Alex has 5, NGO has 3.
        val sowetoMembers = listOf(USER_THABO, USER_LERATO, USER_SIPHO, USER_JABU, USER_NOMSA, USER_KHAYA, USER_ZANELE, USER_BONGANI, USER_TSHEPO, USER_KELE)
        val alexMembers = listOf(USER_NALEDI, USER_RENEILWE, USER_ANDISIWE, USER_MANDLA, USER_SAPS)
        val ngoMembers = listOf(USER_PRIYA, USER_WATER_DEPT, USER_TSHEPO)

        val communities = listOf(
            Community(COMM_SOWETO, "Soweto West Community", "Safety and coordination for Soweto residents.", CommunityType.GEOGRAPHIC, USER_THABO, USER_THABO, listOf(USER_LERATO), "Soweto, Johannesburg", sowetoMembers.size),
            Community(COMM_ALEX, "Alexandra Township Watch", "Community safety for Alexandra.", CommunityType.GEOGRAPHIC, USER_NALEDI, USER_NALEDI, listOf(USER_SAPS), "Alexandra, Johannesburg", alexMembers.size),
            Community(COMM_NGO, "Joburg Infrastructure Monitoring", "Reporting and monitoring infrastructure issues.", CommunityType.ORGANIZATION, USER_PRIYA, USER_PRIYA, emptyList(), "Greater Johannesburg", ngoMembers.size)
        )
        communities.forEach { batch.set(db.collection("communities").document(it.id), it.toFirestoreMap()) }

        // Store memberships in a dedicated collection for easy querying later
        (sowetoMembers.map { it to COMM_SOWETO } + alexMembers.map { it to COMM_ALEX } + ngoMembers.map { it to COMM_NGO }).forEach { (u, c) ->
            val memId = "${u}_${c}"
            batch.set(db.collection("memberships").document(memId), mapOf("userId" to u, "communityId" to c, "joinedAt" to com.google.firebase.Timestamp.now()))
        }

        // 4. REPORTS
        val reports = listOf(
            Report(
                id = RPT_BURST_PIPE,
                reporterId = USER_THABO,
                reporterName = "Thabo Nkosi",
                communityId = COMM_SOWETO,
                communityName = "Soweto West Community",
                title = "Major Water Leak on Vilakazi St",
                description = "A large municipal pipe has burst. Water is flooding the sidewalk and heading toward houses.",
                category = ReportCategory.WATER_SEWAGE,
                urgency = Urgency.HIGH,
                potentialHarm = PotentialHarm.HIGH,
                status = ReportStatus.ESCALATED,
                incidentLocation = IncidentLocation(LocationSource.PHONE_LOCATION, -26.238, 27.909, "8115 Vilakazi St", "South Africa", "Gauteng", "Johannesburg", "Soweto"),
                meTooCount = 5,
                commentCount = 6,
                volunteerCount = 2,
                submittedAt = Instant.now().minus(4, ChronoUnit.HOURS),
                createdAt = Instant.now().minus(4, ChronoUnit.HOURS),
                updatedAt = Instant.now().minus(4, ChronoUnit.HOURS)
            ),
            Report(
                id = RPT_POWER_OUTAGE,
                reporterId = USER_NALEDI,
                reporterName = "Naledi Khumalo",
                communityId = COMM_ALEX,
                communityName = "Alexandra Township Watch",
                title = "Power outage near taxi rank",
                description = "Street lights are out and there is no power in the north block. Dangerous for commuters.",
                category = ReportCategory.INFRASTRUCTURE,
                urgency = Urgency.MEDIUM,
                potentialHarm = PotentialHarm.MODERATE,
                status = ReportStatus.SUBMITTED,
                incidentLocation = IncidentLocation(LocationSource.MANUALLY_DESCRIBED, null, null, "Alexandra Taxi Rank", "South Africa", "Gauteng", "Johannesburg", "Alexandra"),
                meTooCount = 2,
                commentCount = 1,
                volunteerCount = 0,
                submittedAt = Instant.now().minus(2, ChronoUnit.HOURS),
                createdAt = Instant.now().minus(2, ChronoUnit.HOURS),
                updatedAt = Instant.now().minus(2, ChronoUnit.HOURS)
            )
        )
        reports.forEach { batch.set(db.collection("reports").document(it.id), it.toFirestoreMap()) }

        // 4.5 COMMUNITY POSTS (Linking reports to community feeds)
        val communityPosts = listOf(
            CommunityPost(
                id = "post-burst-pipe",
                communityId = COMM_SOWETO,
                authorId = USER_THABO,
                authorName = "Thabo Nkosi",
                communityName = "Soweto West Community",
                reportId = RPT_BURST_PIPE,
                title = "Major Water Leak on Vilakazi St",
                body = "A large municipal pipe has burst. Water is flooding the sidewalk and heading toward houses.",
                category = ReportCategory.WATER_SEWAGE,
                urgency = Urgency.HIGH,
                status = PostStatus.ACTIVE,
                meTooCount = 5,
                commentCount = 6,
                volunteerCount = 2,
                createdAt = Instant.now().minus(4, ChronoUnit.HOURS),
                updatedAt = Instant.now().minus(4, ChronoUnit.HOURS)
            ),
            CommunityPost(
                id = "post-power-outage",
                communityId = COMM_ALEX,
                authorId = USER_NALEDI,
                authorName = "Naledi Khumalo",
                communityName = "Alexandra Township Watch",
                reportId = RPT_POWER_OUTAGE,
                title = "Power outage near taxi rank",
                body = "Street lights are out and there is no power in the north block. Dangerous for commuters.",
                category = ReportCategory.INFRASTRUCTURE,
                urgency = Urgency.MEDIUM,
                status = PostStatus.ACTIVE,
                meTooCount = 2,
                commentCount = 1,
                volunteerCount = 0,
                createdAt = Instant.now().minus(2, ChronoUnit.HOURS),
                updatedAt = Instant.now().minus(2, ChronoUnit.HOURS)
            )
        )
        communityPosts.forEach { batch.set(db.collection("posts").document(it.id), it.toFirestoreMap()) }

        // 5. COMMENTS (For Burst Pipe)
        val pipeComments = listOf(
            Comment(id = "c-001", postId = RPT_BURST_PIPE, authorId = USER_LERATO, authorName = "Lerato Mokoena", parentCommentId = null, body = "I've informed the local ward counselor as well.", createdAt = Instant.now().minus(3, ChronoUnit.HOURS)),
            Comment(id = "c-002", postId = RPT_BURST_PIPE, authorId = USER_SIPHO, authorName = "Sipho Dlamini", parentCommentId = "c-001", body = "Great, thanks Lerato.", createdAt = Instant.now().minus(2, ChronoUnit.HOURS)),
            Comment(id = "c-003", postId = RPT_BURST_PIPE, authorId = USER_JABU, authorName = "Jabu Cele", parentCommentId = null, body = "Water is starting to reach the gate of house 8117.", createdAt = Instant.now().minus(150, ChronoUnit.MINUTES)),
            Comment(id = "c-004", postId = RPT_BURST_PIPE, authorId = USER_NOMSA, authorName = "Nomsa Buthelezi", parentCommentId = null, body = "Is the road still accessible for small cars?", createdAt = Instant.now().minus(2, ChronoUnit.HOURS)),
            Comment(id = "c-005", postId = RPT_BURST_PIPE, authorId = USER_KHAYA, authorName = "Khaya Zondo", parentCommentId = "c-004", body = "Yes, but be careful of the deep pool forming at the corner.", createdAt = Instant.now().minus(90, ChronoUnit.MINUTES)),
            Comment(id = "c-006", postId = RPT_BURST_PIPE, authorId = USER_ZANELE, authorName = "Zanele Sithole", parentCommentId = null, body = "I've just called Joburg Water, they gave me reference number JW12345.", createdAt = Instant.now().minus(1, ChronoUnit.HOURS))
        )
        pipeComments.forEach { batch.set(db.collection("comments").document(it.id), it.toFirestoreMap()) }
        
        val alexComments = listOf(
            Comment(id = "c-007", postId = RPT_POWER_OUTAGE, authorId = USER_SAPS, authorName = "SAPS Alexandra Duty Officer", parentCommentId = null, body = "Patrols increased in the area until lights are restored.", createdAt = Instant.now().minus(1, ChronoUnit.HOURS))
        )
        alexComments.forEach { batch.set(db.collection("comments").document(it.id), it.toFirestoreMap()) }

        // 6. ME TOO (Experiences)
        val meToos = listOf(
            ReportExperience("mt-001", RPT_BURST_PIPE, USER_JABU, "My driveway is flooded.", null, Instant.now().minus(3, ChronoUnit.HOURS)),
            ReportExperience("mt-002", RPT_BURST_PIPE, USER_NOMSA, "I can't get out of my house.", null, Instant.now().minus(160, ChronoUnit.MINUTES)),
            ReportExperience("mt-003", RPT_BURST_PIPE, USER_KHAYA, "The pressure in our house has dropped to zero.", null, Instant.now().minus(2, ChronoUnit.HOURS)),
            ReportExperience("mt-004", RPT_BURST_PIPE, USER_ZANELE, "Saw it on my way to work.", null, Instant.now().minus(100, ChronoUnit.MINUTES)),
            ReportExperience("mt-005", RPT_BURST_PIPE, USER_BONGANI, "Confirmed, it's getting worse.", null, Instant.now().minus(1, ChronoUnit.HOURS)),
            ReportExperience("mt-006", RPT_POWER_OUTAGE, USER_RENEILWE, "Power cut here too.", null, Instant.now().minus(90, ChronoUnit.MINUTES)),
            ReportExperience("mt-007", RPT_POWER_OUTAGE, USER_MANDLA, "Street is very dark.", null, Instant.now().minus(45, ChronoUnit.MINUTES))
        )
        meToos.forEach { batch.set(db.collection("experiences").document(it.id), it.toFirestoreMap()) }

        // 7. VOLUNTEER OFFERS
        val offers = listOf(
            VolunteerOffer(id = "v-001", userId = USER_LERATO, userName = "Lerato Mokoena", reportId = RPT_BURST_PIPE, crisisId = null, postId = null, resourceType = ResourceType.MANPOWER, quantity = 1, note = "I can help redirect traffic away from the water.", status = VolunteerStatus.ACCEPTED),
            VolunteerOffer(id = "v-002", userId = USER_BONGANI, userName = "Bongani Mavuso", reportId = RPT_BURST_PIPE, crisisId = null, postId = null, resourceType = ResourceType.EQUIPMENT, quantity = 1, note = "I have sandbags if anyone needs them for their gates.", status = VolunteerStatus.OFFERED)
        )
        offers.forEach { batch.set(db.collection("volunteerOffers").document(it.id), it.toFirestoreMap()) }

        // 8. CRISES (The Burst Pipe is becoming a crisis)
        val burstCrisis = Crisis(
            id = CRS_WATER_CRISIS,
            title = "Soweto West Water Main Failure",
            description = "Multiple reports of burst pipes leading to widespread water outages and property damage in Soweto West.",
            category = ReportCategory.WATER_SEWAGE,
            status = CrisisStatus.ACTIVE,
            severity = Urgency.HIGH,
            urgency = Urgency.CRITICAL,
            potentialHarm = PotentialHarm.EXTREME,
            incidentLocation = IncidentLocation(LocationSource.PHONE_LOCATION, -26.238, 27.909, "Vilakazi St Area", "South Africa", "Gauteng", "Johannesburg", "Soweto"),
            reportCount = 1,
            independentReporterCount = 1,
            meTooCount = 5,
            peopleAffected = 200,
            manpowerRequired = true,
            resourcesRequired = listOf("PLUMBING_EQUIPMENT", "HEAVY_MACHINERY"),
            responsibleOrganizationId = ORG_WATER,
            detectedAt = Instant.now().minus(4, ChronoUnit.HOURS),
            createdAt = Instant.now().minus(4, ChronoUnit.HOURS),
            updatedAt = Instant.now().minus(4, ChronoUnit.HOURS)
        )
        batch.set(db.collection("crises").document(burstCrisis.id), burstCrisis.toFirestoreMap())

        // 9. OFFICIAL UPDATES
        val updates = listOf(
            OfficialUpdate("u-001", RPT_BURST_PIPE, ORG_WATER, "Joburg Water", "Technicians have been dispatched. Estimated arrival in 45 minutes.", ReportStatus.IN_PROGRESS, Instant.now().minus(3, ChronoUnit.HOURS)),
            OfficialUpdate("u-002", RPT_BURST_PIPE, ORG_WATER, "Joburg Water", "Site isolated. Beginning repairs on the primary 600mm valve.", null, Instant.now().minus(1, ChronoUnit.HOURS))
        )
        updates.forEach { batch.set(db.collection("officialUpdates").document(it.id), it.toFirestoreMap()) }

        // 10. AUDIT EVENTS
        val audits = listOf(
            AuditEvent("a-001", "REPORT", RPT_BURST_PIPE, USER_THABO, AuditAction.REPORT_CREATED, emptyMap(), Instant.now().minus(4, ChronoUnit.HOURS)),
            AuditEvent("a-002", "REPORT", RPT_BURST_PIPE, USER_WATER_DEPT, AuditAction.DEPARTMENT_RESPONDED, mapOf("org" to "Joburg Water"), Instant.now().minus(3, ChronoUnit.HOURS)),
            AuditEvent("a-003", "CRISIS", CRS_WATER_CRISIS, USER_PRIYA, AuditAction.REPORT_UPDATED, mapOf("action" to "ESCALATED_TO_CRISIS"), Instant.now().minus(2, ChronoUnit.HOURS))
        )
        audits.forEach { batch.set(db.collection("auditEvents").document(it.id), it.toFirestoreMap()) }

        batch.commit().await()
    }
}
