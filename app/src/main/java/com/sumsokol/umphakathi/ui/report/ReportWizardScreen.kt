package com.sumsokol.umphakathi.ui.report

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sumsokol.umphakathi.domain.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportWizardScreen(
    communityId: String? = null,
    onBack: () -> Unit,
    onSubmitted: () -> Unit,
    viewModel: ReportWizardViewModel = viewModel(factory = ReportWizardViewModel.factory(communityId))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSubmitted) {
        if (uiState.isSubmitted) onSubmitted()
    }

    val progress = when (uiState.step) {
        WizardStep.CATEGORY -> 0.2f
        WizardStep.DESCRIPTION -> 0.4f
        WizardStep.LOCATION -> 0.6f
        WizardStep.URGENCY -> 0.8f
        WizardStep.REVIEW -> 1.0f
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(when (uiState.step) {
                            WizardStep.CATEGORY -> "What happened?"
                            WizardStep.DESCRIPTION -> "Describe the situation"
                            WizardStep.LOCATION -> "Where did this happen?"
                            WizardStep.URGENCY -> "How urgent is this?"
                            WizardStep.REVIEW -> "Review & Submit"
                        })
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (uiState.step == WizardStep.CATEGORY) onBack()
                            else viewModel.goBack()
                        }) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    }
                )
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (uiState.step) {
                WizardStep.CATEGORY -> CategoryStep(onCategorySelected = viewModel::selectCategory)
                WizardStep.DESCRIPTION -> DescriptionStep(
                    initialTitle = uiState.draft.title,
                    initialDescription = uiState.draft.description,
                    onNext = viewModel::setDescription
                )
                WizardStep.LOCATION -> LocationStep(
                    onPhoneLocation = { viewModel.setLocation(LocationSource.PHONE_LOCATION, city = "Current Location") },
                    onManualDescription = { address, city, neighborhood ->
                        viewModel.setLocation(LocationSource.MANUALLY_DESCRIBED, address = address, city = city, neighborhood = neighborhood)
                    },
                    onSkip = viewModel::skipLocation
                )
                WizardStep.URGENCY -> UrgencyStep(
                    initialUrgency = uiState.draft.urgency,
                    initialHarm = uiState.draft.potentialHarm,
                    onNext = viewModel::setUrgencyAndHarm
                )
                WizardStep.REVIEW -> ReviewStep(
                    draft = uiState.draft,
                    isSubmitting = uiState.isSubmitting,
                    onToggleAnonymous = viewModel::setAnonymous,
                    onSubmit = viewModel::submitReport
                )
            }
        }
    }
}

@Composable
fun CategoryStep(onCategorySelected: (ReportCategory) -> Unit) {
    val categories = ReportCategory.entries

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(categories) { category ->
            CategoryOptionCard(
                icon = getCategoryIcon(category),
                label = getCategoryLabel(category),
                onClick = { onCategorySelected(category) }
            )
        }
    }
}

@Composable
fun CategoryOptionCard(icon: ImageVector, label: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.aspectRatio(1f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun DescriptionStep(
    initialTitle: String,
    initialDescription: String,
    onNext: (String, String) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }
    val canContinue = title.isNotBlank() && description.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Provide a clear title and description of what happened.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title *") },
            placeholder = { Text("Brief summary of the situation") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description *") },
            placeholder = { Text("Describe what happened in detail. Who is affected? What is the current situation?") },
            modifier = Modifier.fillMaxWidth().height(160.dp),
            maxLines = 8
        )
        Button(
            onClick = { onNext(title, description) },
            modifier = Modifier.fillMaxWidth(),
            enabled = canContinue
        ) { Text("Continue") }
    }
}

@Composable
fun LocationStep(
    onPhoneLocation: () -> Unit,
    onManualDescription: (String, String, String) -> Unit,
    onSkip: () -> Unit
) {
    var showManualEntry by remember { mutableStateOf(false) }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var neighborhood by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Where did this incident occur? You are sharing the incident location, not your personal location.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Card(onClick = onPhoneLocation, modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MyLocation, null, Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Share incident location", fontWeight = FontWeight.SemiBold)
                    Text("Uses your current phone location to pinpoint where the incident is happening",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Card(onClick = { showManualEntry = !showManualEntry }, modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.EditLocation, null, Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Describe location manually", fontWeight = FontWeight.SemiBold)
                    Text("Type the address, area or landmark",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (showManualEntry) {
            OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address / description") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = neighborhood, onValueChange = { neighborhood = it }, label = { Text("Neighborhood (optional)") }, modifier = Modifier.fillMaxWidth())
            Button(
                onClick = { onManualDescription(address, city, neighborhood) },
                modifier = Modifier.fillMaxWidth(),
                enabled = address.isNotBlank() || city.isNotBlank()
            ) { Text("Continue") }
        }
        OutlinedButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.LocationOff, null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Skip location")
        }
    }
}

@Composable
fun UrgencyStep(
    initialUrgency: Urgency,
    initialHarm: PotentialHarm,
    onNext: (Urgency, PotentialHarm) -> Unit
) {
    var urgency by remember { mutableStateOf(initialUrgency) }
    var potentialHarm by remember { mutableStateOf(initialHarm) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Urgency Level", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Urgency.entries.forEach { u ->
            val selected = urgency == u
            Card(
                onClick = { urgency = u },
                colors = CardDefaults.cardColors(
                    containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selected, onClick = { urgency = u })
                    Column {
                        Text(u.name, fontWeight = FontWeight.SemiBold)
                        Text(
                            when(u) {
                                Urgency.LOW -> "Not time-sensitive"
                                Urgency.MEDIUM -> "Should be addressed soon"
                                Urgency.HIGH -> "Needs attention quickly"
                                Urgency.CRITICAL -> "Immediate action required"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        Text("Potential Harm", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        PotentialHarm.entries.forEach { h ->
            val selected = potentialHarm == h
            Card(
                onClick = { potentialHarm = h },
                colors = CardDefaults.cardColors(
                    containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selected, onClick = { potentialHarm = h })
                    Text(h.name, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Button(onClick = { onNext(urgency, potentialHarm) }, modifier = Modifier.fillMaxWidth()) {
            Text("Review Report")
        }
    }
}

@Composable
fun ReviewStep(
    draft: ReportDraft,
    isSubmitting: Boolean,
    onToggleAnonymous: (Boolean) -> Unit,
    onSubmit: () -> Unit
) {
    val sensitiveCategories = listOf(ReportCategory.ABUSE, ReportCategory.VIOLENCE, ReportCategory.MISSING_PERSON, ReportCategory.CRIME)
    val isSensitive = sensitiveCategories.contains(draft.category)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Review your report before submitting",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        ReviewRow(label = "Category", value = draft.category?.let { getCategoryLabel(it) } ?: "")
        ReviewRow(label = "Title", value = draft.title)
        ReviewRow(label = "Description", value = draft.description)
        ReviewRow(label = "Location", value = when(draft.locationSource) {
            LocationSource.PHONE_LOCATION -> "Current phone location"
            LocationSource.MANUALLY_DESCRIBED -> listOfNotNull(draft.addressDescription.ifBlank { null }, draft.city.ifBlank { null }).joinToString(", ")
            LocationSource.NOT_PROVIDED -> "Not provided"
            else -> "Map selection"
        })
        ReviewRow(label = "Urgency", value = draft.urgency.name)
        ReviewRow(label = "Potential Harm", value = draft.potentialHarm.name)

        Spacer(Modifier.height(8.dp))
        
        // Anonymous Toggle
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (draft.isAnonymous) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (draft.isAnonymous) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    null,
                    Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Report Anonymously", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Text(
                        if (isSensitive) "Highly recommended for this category. Your identity will be hidden from everyone."
                        else "Hide your identity from other users and organizations.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Switch(
                    checked = draft.isAnonymous,
                    onCheckedChange = onToggleAnonymous
                )
            }
        }

        if (draft.category != null) {
            Spacer(Modifier.height(8.dp))
            Text("Clear Next Steps", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text("Even before you submit, consider taking these actions:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            getNextStepsForCategory(draft.category).forEach { step ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(step.title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text(step.description, style = MaterialTheme.typography.bodySmall)
                        }
                        step.hotline?.let {
                            IconButton(onClick = { /* Call hotline */ }) {
                                Icon(Icons.Default.Phone, "Call", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSubmitting
        ) {
            if (isSubmitting) { CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp) }
            else { Text("Submit Report") }
        }
    }
}

@Composable
fun ReviewRow(label: String, value: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.width(100.dp))
            Text(value, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        }
    }
}
