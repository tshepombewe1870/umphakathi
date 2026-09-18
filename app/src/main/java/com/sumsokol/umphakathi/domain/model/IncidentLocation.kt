package com.sumsokol.umphakathi.domain.model

enum class LocationSource {
    PHONE_LOCATION,
    MANUALLY_SELECTED,
    MANUALLY_DESCRIBED,
    NOT_PROVIDED
}

enum class LocationVisibility {
    PUBLIC_EXACT,
    PUBLIC_APPROXIMATE,
    RESPONDERS_ONLY,
    PRIVATE
}

data class IncidentLocation(
    val source: LocationSource = LocationSource.NOT_PROVIDED,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val addressDescription: String? = null,
    val country: String? = null,
    val province: String? = null,
    val city: String? = null,
    val neighborhood: String? = null
)
