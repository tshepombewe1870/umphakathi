package com.sumsokol.umphakathi.data.firebase

import com.sumsokol.umphakathi.domain.model.*
import java.util.Date

internal fun Report.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "reporterId" to reporterId,
    "reporterName" to reporterName,
    "reporterAvatar" to reporterAvatar,
    "crisisId" to crisisId,
    "communityId" to communityId,
    "communityName" to communityName,
    "title" to title,
    "description" to description,
    "category" to category.name,
    "urgency" to urgency.name,
    "potentialHarm" to potentialHarm.name,
    "status" to status.name,
    "locationSource" to incidentLocation.source.name,
    "latitude" to incidentLocation.latitude,
    "longitude" to incidentLocation.longitude,
    "addressDescription" to incidentLocation.addressDescription,
    "country" to incidentLocation.country,
    "province" to incidentLocation.province,
    "city" to incidentLocation.city,
    "neighborhood" to incidentLocation.neighborhood,
    "locationVisibility" to locationVisibility.name,
    "incidentStartedAt" to incidentStartedAt?.let { com.google.firebase.Timestamp(Date.from(it)) },
    "submittedAt" to com.google.firebase.Timestamp(Date.from(submittedAt)),
    "meTooCount" to meTooCount,
    "commentCount" to commentCount,
    "volunteerCount" to volunteerCount,
    "shareCount" to shareCount,
    "likeCount" to likeCount,
    "createdAt" to com.google.firebase.Timestamp(Date.from(createdAt)),
    "updatedAt" to com.google.firebase.Timestamp(Date.from(updatedAt))
)

internal fun Crisis.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "title" to title,
    "description" to description,
    "category" to category.name,
    "status" to status.name,
    "severity" to severity.name,
    "urgency" to urgency.name,
    "potentialHarm" to potentialHarm.name,
    "locationSource" to incidentLocation.source.name,
    "latitude" to incidentLocation.latitude,
    "longitude" to incidentLocation.longitude,
    "addressDescription" to incidentLocation.addressDescription,
    "country" to incidentLocation.country,
    "province" to incidentLocation.province,
    "city" to incidentLocation.city,
    "neighborhood" to incidentLocation.neighborhood,
    "reportCount" to reportCount,
    "independentReporterCount" to independentReporterCount,
    "meTooCount" to meTooCount,
    "peopleAffected" to peopleAffected,
    "manpowerRequired" to manpowerRequired,
    "resourcesRequired" to resourcesRequired,
    "responsibleOrganizationId" to responsibleOrganizationId,
    "detectedAt" to com.google.firebase.Timestamp(Date.from(detectedAt)),
    "createdAt" to com.google.firebase.Timestamp(Date.from(createdAt)),
    "updatedAt" to com.google.firebase.Timestamp(Date.from(updatedAt)),
    "resolvedAt" to resolvedAt?.let { com.google.firebase.Timestamp(Date.from(it)) }
)

internal fun Community.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "name" to name,
    "description" to description,
    "type" to type.name,
    "creatorId" to creatorId,
    "ownerId" to ownerId,
    "moderatorIds" to moderatorIds,
    "location" to location,
    "memberCount" to memberCount,
    "createdAt" to com.google.firebase.Timestamp(Date.from(createdAt)),
    "updatedAt" to com.google.firebase.Timestamp(Date.from(updatedAt)),
    "isActive" to isActive
)

internal fun CommunityPost.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "communityId" to communityId,
    "authorId" to authorId,
    "authorName" to authorName,
    "communityName" to communityName,
    "reportId" to reportId,
    "crisisId" to crisisId,
    "title" to title,
    "body" to body,
    "category" to category.name,
    "urgency" to urgency.name,
    "status" to status.name,
    "commentCount" to commentCount,
    "meTooCount" to meTooCount,
    "volunteerCount" to volunteerCount,
    "createdAt" to com.google.firebase.Timestamp(Date.from(createdAt)),
    "updatedAt" to com.google.firebase.Timestamp(Date.from(updatedAt))
)

internal fun Comment.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "reportId" to postId,
    "authorId" to authorId,
    "authorName" to authorName,
    "parentCommentId" to parentCommentId,
    "body" to body,
    "likeCount" to likeCount,
    "createdAt" to com.google.firebase.Timestamp(Date.from(createdAt)),
    "updatedAt" to com.google.firebase.Timestamp(Date.from(updatedAt)),
    "isDeleted" to isDeleted
)

internal fun Resolution.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "reportId" to reportId,
    "resolvedBy" to resolvedBy,
    "method" to method.name,
    "explanation" to explanation,
    "corroborationCount" to corroborationCount,
    "createdAt" to com.google.firebase.Timestamp(Date.from(createdAt))
)

internal fun Notice.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "communityId" to communityId,
    "creatorId" to creatorId,
    "creatorName" to creatorName,
    "creatorType" to creatorType,
    "title" to title,
    "body" to body,
    "type" to type.name,
    "status" to status.name,
    "startTime" to startTime?.let { com.google.firebase.Timestamp(Date.from(it)) },
    "endTime" to endTime?.let { com.google.firebase.Timestamp(Date.from(it)) },
    "locationDescription" to locationDescription,
    "createdAt" to com.google.firebase.Timestamp(Date.from(createdAt)),
    "updatedAt" to com.google.firebase.Timestamp(Date.from(updatedAt))
)

internal fun VolunteerOffer.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "userId" to userId,
    "userName" to userName,
    "reportId" to reportId,
    "crisisId" to crisisId,
    "postId" to postId,
    "resourceType" to resourceType.name,
    "quantity" to quantity,
    "note" to note,
    "status" to status.name,
    "likeCount" to likeCount,
    "commentCount" to commentCount,
    "createdAt" to com.google.firebase.Timestamp(Date.from(createdAt)),
    "updatedAt" to com.google.firebase.Timestamp(Date.from(updatedAt))
)

internal fun OfficialUpdate.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "reportId" to reportId,
    "organizationId" to organizationId,
    "organizationName" to organizationName,
    "message" to message,
    "statusUpdate" to statusUpdate?.name,
    "createdAt" to com.google.firebase.Timestamp(Date.from(createdAt))
)

internal fun AuditEvent.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "entityType" to entityType,
    "entityId" to entityId,
    "actorId" to actorId,
    "action" to action.name,
    "metadata" to metadata,
    "createdAt" to com.google.firebase.Timestamp(Date.from(createdAt))
)

internal fun User.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "username" to username,
    "accountType" to accountType.name,
    "role" to role,
    "communityName" to communityName,
    "verified" to verified,
    "createdAt" to com.google.firebase.Timestamp(Date.from(createdAt)),
    "updatedAt" to com.google.firebase.Timestamp(Date.from(updatedAt)),
    "isActive" to isActive
)

internal fun Organization.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "accountUserId" to accountUserId,
    "name" to name,
    "organizationType" to organizationType.name,
    "verified" to verified,
    "description" to description,
    "serviceAreas" to serviceAreas,
    "createdAt" to com.google.firebase.Timestamp(Date.from(createdAt))
)

internal fun ReportExperience.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "reportId" to reportId,
    "userId" to userId,
    "userName" to userName,
    "description" to description,
    "incidentStartedAt" to incidentStartedAt?.let { com.google.firebase.Timestamp(Date.from(it)) },
    "createdAt" to com.google.firebase.Timestamp(Date.from(createdAt))
)

internal fun CrisisSignal.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "crisisId" to crisisId,
    "userId" to userId,
    "type" to type.name,
    "createdAt" to com.google.firebase.Timestamp(Date.from(createdAt))
)

// --- Mappings from Firestore Document to Domain Models ---

internal fun com.google.firebase.firestore.DocumentSnapshot.toReport(): Report? = try {
    Report(
        id = getString("id") ?: id,
        reporterId = getString("reporterId") ?: "",
        reporterName = getString("reporterName") ?: "Anonymous",
        reporterAvatar = getString("reporterAvatar"),
        crisisId = getString("crisisId"),
        communityId = getString("communityId"),
        communityName = getString("communityName"),
        title = getString("title") ?: "",
        description = getString("description") ?: "",
        category = ReportCategory.valueOf(getString("category") ?: "OTHER"),
        urgency = Urgency.valueOf(getString("urgency") ?: "MEDIUM"),
        potentialHarm = PotentialHarm.valueOf(getString("potentialHarm") ?: "MODERATE"),
        status = ReportStatus.valueOf(getString("status") ?: "SUBMITTED"),
        incidentLocation = IncidentLocation(
            source = LocationSource.valueOf(getString("locationSource") ?: "NOT_PROVIDED"),
            latitude = getDouble("latitude"),
            longitude = getDouble("longitude"),
            addressDescription = getString("addressDescription"),
            country = getString("country"),
            province = getString("province"),
            city = getString("city"),
            neighborhood = getString("neighborhood")
        ),
        locationVisibility = LocationVisibility.valueOf(getString("locationVisibility") ?: "PUBLIC_APPROXIMATE"),
        incidentStartedAt = getTimestamp("incidentStartedAt")?.toDate()?.toInstant(),
        submittedAt = getTimestamp("submittedAt")?.toDate()?.toInstant() ?: java.time.Instant.now(),
        meTooCount = getLong("meTooCount")?.toInt() ?: 0,
        commentCount = getLong("commentCount")?.toInt() ?: 0,
        volunteerCount = getLong("volunteerCount")?.toInt() ?: 0,
        shareCount = getLong("shareCount")?.toInt() ?: 0,
        likeCount = getLong("likeCount")?.toInt() ?: 0,
        createdAt = getTimestamp("createdAt")?.toDate()?.toInstant() ?: java.time.Instant.now(),
        updatedAt = getTimestamp("updatedAt")?.toDate()?.toInstant() ?: java.time.Instant.now()
    )
} catch (e: Exception) { null }

internal fun com.google.firebase.firestore.DocumentSnapshot.toCrisis(): Crisis? = try {
    Crisis(
        id = getString("id") ?: id,
        title = getString("title") ?: "",
        description = getString("description") ?: "",
        category = ReportCategory.valueOf(getString("category") ?: "OTHER"),
        status = CrisisStatus.valueOf(getString("status") ?: "IDENTIFIED"),
        severity = Urgency.valueOf(getString("severity") ?: "MEDIUM"),
        urgency = Urgency.valueOf(getString("urgency") ?: "MEDIUM"),
        potentialHarm = PotentialHarm.valueOf(getString("potentialHarm") ?: "MODERATE"),
        incidentLocation = IncidentLocation(
            source = LocationSource.valueOf(getString("locationSource") ?: "NOT_PROVIDED"),
            latitude = getDouble("latitude"),
            longitude = getDouble("longitude"),
            addressDescription = getString("addressDescription"),
            country = getString("country"),
            province = getString("province"),
            city = getString("city"),
            neighborhood = getString("neighborhood")
        ),
        reportCount = getLong("reportCount")?.toInt() ?: 0,
        independentReporterCount = getLong("independentReporterCount")?.toInt() ?: 0,
        meTooCount = getLong("meTooCount")?.toInt() ?: 0,
        peopleAffected = getLong("peopleAffected")?.toInt(),
        manpowerRequired = getBoolean("manpowerRequired") ?: false,
        resourcesRequired = (get("resourcesRequired") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
        responsibleOrganizationId = getString("responsibleOrganizationId"),
        detectedAt = getTimestamp("detectedAt")?.toDate()?.toInstant() ?: java.time.Instant.now(),
        createdAt = getTimestamp("createdAt")?.toDate()?.toInstant() ?: java.time.Instant.now(),
        updatedAt = getTimestamp("updatedAt")?.toDate()?.toInstant() ?: java.time.Instant.now(),
        resolvedAt = getTimestamp("resolvedAt")?.toDate()?.toInstant()
    )
} catch (e: Exception) { null }

internal fun com.google.firebase.firestore.DocumentSnapshot.toUser(): User? = try {
    val username = getString("username")
    User(
        id = getString("id") ?: id,
        username = if (username.isNullOrBlank()) "user_${id.take(4)}" else username,
        accountType = AccountType.valueOf(getString("accountType") ?: "PERSON"),
        role = getString("role"),
        communityName = getString("communityName"),
        verified = getBoolean("verified") ?: false,
        createdAt = getTimestamp("createdAt")?.toDate()?.toInstant() ?: java.time.Instant.now(),
        updatedAt = getTimestamp("updatedAt")?.toDate()?.toInstant() ?: java.time.Instant.now(),
        isActive = getBoolean("isActive") ?: true
    )
} catch (e: Exception) { null }

internal fun com.google.firebase.firestore.DocumentSnapshot.toCommunity(): Community? = try {
    Community(
        id = getString("id") ?: id,
        name = getString("name") ?: "",
        description = getString("description") ?: "",
        type = CommunityType.valueOf(getString("type") ?: "OTHER"),
        creatorId = getString("creatorId") ?: "",
        ownerId = getString("ownerId") ?: "",
        moderatorIds = (get("moderatorIds") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
        location = getString("location"),
        memberCount = getLong("memberCount")?.toInt() ?: 0,
        createdAt = getTimestamp("createdAt")?.toDate()?.toInstant() ?: java.time.Instant.now(),
        updatedAt = getTimestamp("updatedAt")?.toDate()?.toInstant() ?: java.time.Instant.now(),
        isActive = getBoolean("isActive") ?: true
    )
} catch (e: Exception) { null }

internal fun com.google.firebase.firestore.DocumentSnapshot.toPost(): CommunityPost? = try {
    CommunityPost(
        id = getString("id") ?: id,
        communityId = getString("communityId") ?: "",
        authorId = getString("authorId") ?: "",
        authorName = getString("authorName") ?: "Anonymous",
        communityName = getString("communityName"),
        reportId = getString("reportId"),
        crisisId = getString("crisisId"),
        title = getString("title") ?: "",
        body = getString("body") ?: "",
        category = ReportCategory.valueOf(getString("category") ?: "OTHER"),
        urgency = Urgency.valueOf(getString("urgency") ?: "MEDIUM"),
        status = PostStatus.valueOf(getString("status") ?: "ACTIVE"),
        commentCount = getLong("commentCount")?.toInt() ?: 0,
        meTooCount = getLong("meTooCount")?.toInt() ?: 0,
        volunteerCount = getLong("volunteerCount")?.toInt() ?: 0,
        createdAt = getTimestamp("createdAt")?.toDate()?.toInstant() ?: java.time.Instant.now(),
        updatedAt = getTimestamp("updatedAt")?.toDate()?.toInstant() ?: java.time.Instant.now()
    )
} catch (e: Exception) { null }

internal fun com.google.firebase.firestore.DocumentSnapshot.toComment(): Comment? = try {
    Comment(
        id = getString("id") ?: id,
        postId = getString("postId") ?: getString("reportId") ?: "",
        authorId = getString("authorId") ?: "",
        authorName = getString("authorName") ?: "Anonymous",
        parentCommentId = getString("parentCommentId"),
        body = getString("body") ?: "",
        likeCount = getLong("likeCount")?.toInt() ?: 0,
        createdAt = getTimestamp("createdAt")?.toDate()?.toInstant() ?: java.time.Instant.now(),
        updatedAt = getTimestamp("updatedAt")?.toDate()?.toInstant() ?: java.time.Instant.now(),
        isDeleted = getBoolean("isDeleted") ?: false
    )
} catch (e: Exception) { null }

internal fun com.google.firebase.firestore.DocumentSnapshot.toVolunteerOffer(): VolunteerOffer? = try {
    val userName = getString("userName")
    VolunteerOffer(
        id = getString("id") ?: id,
        userId = getString("userId") ?: "",
        userName = if (userName.isNullOrBlank()) "Anonymous" else userName,
        reportId = getString("reportId"),
        crisisId = getString("crisisId"),
        postId = getString("postId"),
        resourceType = ResourceType.valueOf(getString("resourceType") ?: "MANPOWER"),
        quantity = getLong("quantity")?.toInt(),
        note = getString("note"),
        status = VolunteerStatus.valueOf(getString("status") ?: "OFFERED"),
        likeCount = getLong("likeCount")?.toInt() ?: 0,
        commentCount = getLong("commentCount")?.toInt() ?: 0,
        createdAt = getTimestamp("createdAt")?.toDate()?.toInstant() ?: java.time.Instant.now(),
        updatedAt = getTimestamp("updatedAt")?.toDate()?.toInstant() ?: java.time.Instant.now()
    )
} catch (e: Exception) { null }

internal fun com.google.firebase.firestore.DocumentSnapshot.toOfficialUpdate(): OfficialUpdate? = try {
    val orgName = getString("organizationName")
    OfficialUpdate(
        id = getString("id") ?: id,
        reportId = getString("reportId") ?: "",
        organizationId = getString("organizationId") ?: "",
        organizationName = if (orgName.isNullOrBlank()) "Organization" else orgName,
        message = getString("message") ?: "",
        statusUpdate = getString("statusUpdate")?.let { ReportStatus.valueOf(it) },
        createdAt = getTimestamp("createdAt")?.toDate()?.toInstant() ?: java.time.Instant.now()
    )
} catch (e: Exception) { null }

internal fun com.google.firebase.firestore.DocumentSnapshot.toAuditEvent(): AuditEvent? = try {
    AuditEvent(
        id = getString("id") ?: id,
        entityType = getString("entityType") ?: "",
        entityId = getString("entityId") ?: "",
        actorId = getString("actorId") ?: "",
        action = AuditAction.valueOf(getString("action") ?: "REPORT_UPDATED"),
        metadata = (get("metadata") as? Map<*, *>)?.map { it.key.toString() to it.value.toString() }?.toMap() ?: emptyMap(),
        createdAt = getTimestamp("createdAt")?.toDate()?.toInstant() ?: java.time.Instant.now()
    )
} catch (e: Exception) { null }

internal fun com.google.firebase.firestore.DocumentSnapshot.toReportExperience(): ReportExperience? = try {
    val userName = getString("userName")
    ReportExperience(
        id = getString("id") ?: id,
        reportId = getString("reportId") ?: "",
        userId = getString("userId") ?: "",
        userName = if (userName.isNullOrBlank()) "Anonymous" else userName,
        description = getString("description"),
        incidentStartedAt = getTimestamp("incidentStartedAt")?.toDate()?.toInstant(),
        createdAt = getTimestamp("createdAt")?.toDate()?.toInstant() ?: java.time.Instant.now()
    )
} catch (e: Exception) { null }

internal fun com.google.firebase.firestore.DocumentSnapshot.toNotice(): Notice? = try {
    Notice(
        id = getString("id") ?: id,
        communityId = getString("communityId") ?: "",
        creatorId = getString("creatorId") ?: "",
        creatorName = getString("creatorName") ?: "",
        creatorType = getString("creatorType") ?: "PERSON",
        title = getString("title") ?: "",
        body = getString("body") ?: "",
        type = NoticeType.valueOf(getString("type") ?: "COMMUNITY_EVENT"),
        status = NoticeStatus.valueOf(getString("status") ?: "PENDING_APPROVAL"),
        startTime = getTimestamp("startTime")?.toDate()?.toInstant(),
        endTime = getTimestamp("endTime")?.toDate()?.toInstant(),
        locationDescription = getString("locationDescription"),
        createdAt = getTimestamp("createdAt")?.toDate()?.toInstant() ?: java.time.Instant.now(),
        updatedAt = getTimestamp("updatedAt")?.toDate()?.toInstant() ?: java.time.Instant.now()
    )
} catch (e: Exception) { null }

internal fun com.google.firebase.firestore.DocumentSnapshot.toOrganization(): Organization? = try {
    Organization(
        id = getString("id") ?: id,
        accountUserId = getString("accountUserId") ?: "",
        name = getString("name") ?: "",
        organizationType = OrganizationType.valueOf(getString("organizationType") ?: "OTHER"),
        verified = getBoolean("verified") ?: false,
        description = getString("description"),
        serviceAreas = (get("serviceAreas") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
        createdAt = getTimestamp("createdAt")?.toDate()?.toInstant() ?: java.time.Instant.now()
    )
} catch (e: Exception) { null }
