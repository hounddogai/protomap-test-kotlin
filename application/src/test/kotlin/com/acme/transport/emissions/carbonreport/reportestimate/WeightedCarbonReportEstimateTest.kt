package com.acme.transport.emissions.carbonreport.reportestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedCarbonReportEstimateTest {
    @Test
    fun `starts in idle`() {
        val item = newWeightedCarbonReportEstimate("code-603", "name-769")
        assertEquals(WeightedCarbonReportEstimateStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newWeightedCarbonReportEstimate("code-603", "name-769")
        try {
            item.moveTo(WeightedCarbonReportEstimateStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedCarbonReportEstimateStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newWeightedCarbonReportEstimate("code-603", "name-769")
        item.status = WeightedCarbonReportEstimateStatus.STOPPED
        assertFails("move") { item.moveTo(WeightedCarbonReportEstimateStatus.IDLE) }
    }
}
