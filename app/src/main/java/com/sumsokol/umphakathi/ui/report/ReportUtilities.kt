package com.sumsokol.umphakathi.ui.report

import com.sumsokol.umphakathi.domain.model.ReportCategory

data class NextStep(
    val title: String,
    val description: String,
    val hotline: String? = null
)

fun getNextStepsForCategory(category: ReportCategory): List<NextStep> {
    return when (category) {
        ReportCategory.ABUSE -> listOf(
            NextStep("Childline South Africa", "Confidential service for children and families.", "0800 055 555"),
            NextStep("Gender-Based Violence Command Centre", "Support for victims of gender-based violence.", "0800 428 428"),
            NextStep("SAPS Emergency", "Contact the police for immediate danger.", "10111")
        )
        ReportCategory.VIOLENCE -> listOf(
            NextStep("SAPS Emergency", "Report active violence to the police immediately.", "10111"),
            NextStep("Crime Stop", "Report crime anonymously.", "08600 10111")
        )
        ReportCategory.MISSING_PERSON -> listOf(
            NextStep("SAPS Missing Persons", "There is no 24-hour waiting period to report a missing person.", "10111"),
            NextStep("Missing Children SA", "Report missing children to this specialized NGO.", "082 890 2040")
        )
        ReportCategory.MEDICAL_EMERGENCY -> listOf(
            NextStep("ER24 / Netcare 911", "Private emergency medical services.", "084 124 / 911"),
            NextStep("State Ambulance", "Public emergency medical services.", "10177")
        )
        ReportCategory.INFRASTRUCTURE, ReportCategory.WATER_SEWAGE -> listOf(
            NextStep("City of Johannesburg (Joburg Water)", "Report burst pipes and leaks.", "011 375 5555"),
            NextStep("City Power", "Report power outages and dangerous cabling.", "011 375 5555")
        )
        ReportCategory.FIRE -> listOf(
            NextStep("Fire Department", "Report active fires immediately.", "10177"),
            NextStep("General Emergency", "Cell phone emergency number.", "112")
        )
        ReportCategory.NATURAL_DISASTER -> listOf(
            NextStep("Disaster Management", "Contact regional disaster management teams.", "011 375 5911")
        )
        ReportCategory.CRIME -> listOf(
            NextStep("SAPS", "Report criminal activity.", "10111"),
            NextStep("Crime Stop", "Share anonymous tips about crime.", "08600 10111")
        )
        else -> listOf(
            NextStep("General Emergency", "For any life-threatening emergency.", "112"),
            NextStep("Local Ward Councilor", "Contact your community representative for municipal issues.")
        )
    }
}
