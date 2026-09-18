package com.sumsokol.umphakathi

import com.sumsokol.umphakathi.domain.model.LocationSource
import com.sumsokol.umphakathi.domain.model.PotentialHarm
import com.sumsokol.umphakathi.domain.model.ReportCategory
import com.sumsokol.umphakathi.domain.model.Urgency
import com.sumsokol.umphakathi.ui.report.ReportWizardViewModel
import com.sumsokol.umphakathi.ui.report.WizardStep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReportWizardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: ReportWizardViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ReportWizardViewModel(communityId = "community-001")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStep_isCategory() {
        assertEquals(WizardStep.CATEGORY, viewModel.uiState.value.step)
        assertEquals("community-001", viewModel.uiState.value.draft.communityId)
    }

    @Test
    fun selectCategory_advancesToDescriptionStep() {
        viewModel.selectCategory(ReportCategory.WATER_SEWAGE)

        assertEquals(WizardStep.DESCRIPTION, viewModel.uiState.value.step)
        assertEquals(ReportCategory.WATER_SEWAGE, viewModel.uiState.value.draft.category)
    }

    @Test
    fun wizardFlow_completesAllSteps() = runTest {
        // Step 1: Category
        viewModel.selectCategory(ReportCategory.FIRE)
        assertEquals(WizardStep.DESCRIPTION, viewModel.uiState.value.step)

        // Step 2: Description
        viewModel.setDescription("House fire on 4th Ave", "Flames visible from roof")
        assertEquals(WizardStep.LOCATION, viewModel.uiState.value.step)
        assertEquals("House fire on 4th Ave", viewModel.uiState.value.draft.title)

        // Step 3: Location
        viewModel.setLocation(LocationSource.MANUALLY_DESCRIBED, address = "123 4th Ave", city = "Soweto")
        assertEquals(WizardStep.URGENCY, viewModel.uiState.value.step)

        // Step 4: Urgency & Harm
        viewModel.setUrgencyAndHarm(Urgency.CRITICAL, PotentialHarm.EXTREME)
        assertEquals(WizardStep.REVIEW, viewModel.uiState.value.step)

        // Step 5: Submit
        viewModel.submitReport()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSubmitted)
        assertFalse(viewModel.uiState.value.isSubmitting)
    }

    @Test
    fun goBack_navigatesToPreviousStep() {
        viewModel.selectCategory(ReportCategory.ABUSE)
        assertEquals(WizardStep.DESCRIPTION, viewModel.uiState.value.step)

        viewModel.goBack()
        assertEquals(WizardStep.CATEGORY, viewModel.uiState.value.step)
    }
}
