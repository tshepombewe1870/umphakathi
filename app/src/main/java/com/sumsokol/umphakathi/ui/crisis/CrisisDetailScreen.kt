package com.sumsokol.umphakathi.ui.crisis

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sumsokol.umphakathi.domain.model.*
import com.sumsokol.umphakathi.ui.report.IncidentCard
import com.sumsokol.umphakathi.ui.report.VolunteerOfferCard
import com.sumsokol.umphakathi.ui.report.StatusChip
import com.sumsokol.umphakathi.ui.report.UrgencyChip
import com.sumsokol.umphakathi.ui.report.CategoryChip
import com.sumsokol.umphakathi.ui.report.VolunteerOfferDialog
import com.sumsokol.umphakathi.ui.report.timeAgo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrisisDetailScreen(
    crisisId: String,
    onBack: () -> Unit,
    viewModel: CrisisDetailViewModel = viewModel(key = crisisId, factory = CrisisDetailViewModel.factory(crisisId))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showVolunteerDialog by remember { mutableStateOf(false) }

    if (showVolunteerDialog) {
        VolunteerOfferDialog(
            onDismiss = { showVolunteerDialog = false },
            onSubmit = { types, note ->
                viewModel.submitVolunteerOffer(types, note)
                showVolunteerDialog = false
            }
        )
    }

    Scaffold { paddingValues ->
        val crisis = uiState.crisis
        if (uiState.isLoading || crisis == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // Severity banner
                item {
                    val bannerColor = when (crisis.potentialHarm) {
                        PotentialHarm.EXTREME -> Color(0xFFD32F2F)
                        PotentialHarm.HIGH -> Color(0xFFE64A19)
                        PotentialHarm.MODERATE -> Color(0xFFFFA000)
                        PotentialHarm.LOW -> Color(0xFF388E3C)
                    }
                    Surface(color = bannerColor, modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, null, Modifier.size(20.dp), tint = Color.White)
                                Spacer(Modifier.width(8.dp))
                                Text(crisis.status.name.replace('_', ' '), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                "${crisis.potentialHarm.name} HARM",
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            CategoryChip(category = crisis.category)
                            Spacer(Modifier.height(8.dp))
                            Text(crisis.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Text(crisis.description, style = MaterialTheme.typography.bodyMedium)
                            crisis.incidentLocation.city?.let { city ->
                                Spacer(Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        listOfNotNull(crisis.incidentLocation.neighborhood, city).joinToString(", "),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }

                // Stats
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            CrisisStat(label = "Reports", value = crisis.reportCount.toString())
                            CrisisStat(label = "Reporters", value = crisis.independentReporterCount.toString())
                            CrisisStat(label = "Me Too", value = crisis.meTooCount.toString())
                            crisis.peopleAffected?.let { CrisisStat(label = "Affected", value = it.toString()) }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                // Help button
                item {
                    Button(
                        onClick = { showVolunteerDialog = true },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    ) {
                        Icon(Icons.Default.VolunteerActivism, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Offer Help / Volunteer")
                    }
                    Spacer(Modifier.height(12.dp))
                }

                // Volunteer offers
                if (uiState.volunteerOffers.isNotEmpty()) {
                    item {
                        Text(
                            "Volunteer Offers (${uiState.volunteerOffers.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    items(uiState.volunteerOffers) { offer ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                            VolunteerOfferCard(offer = offer)
                        }
                    }
                    item { Spacer(Modifier.height(12.dp)) }
                }

                // Linked reports
                if (uiState.reports.isNotEmpty()) {
                    item {
                        Text(
                            "Linked Reports (${uiState.reports.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    items(uiState.reports) { report ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                            IncidentCard(
                                reportId = report.id,
                                title = report.title,
                                description = report.description,
                                status = report.status,
                                category = report.category,
                                urgency = report.urgency,
                                submittedAt = report.submittedAt,
                                neighborhood = report.incidentLocation.neighborhood,
                                meTooCount = report.meTooCount,
                                commentCount = report.commentCount,
                                volunteerCount = report.volunteerCount,
                                authorName = report.reporterName,
                                communityName = report.communityName,
                                imageUrls = report.imageUrls,
                                onClick = { /* Navigate to report detail */ },
                                onMeToo = { /* Corroborate */ }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CrisisStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
    }
}
