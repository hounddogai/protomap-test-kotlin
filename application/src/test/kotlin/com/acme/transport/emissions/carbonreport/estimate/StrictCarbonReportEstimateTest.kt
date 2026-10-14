package com.acme.transport.emissions.carbonreport.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictCarbonReportEstimateTest {
    @Test
    fun `starts in planned`() {
        val item = newStrictCarbonReportEstimate("code-444", "label-792")
        assertEquals(StrictCarbonReportEstimateStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newStrictCarbonReportEstimate("code-444", "label-792")
        try {
            item.moveTo(StrictCarbonReportEstimateStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictCarbonReportEstimateStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newStrictCarbonReportEstimate("code-444", "label-792")
        item.status = StrictCarbonReportEstimateStatus.CANCELLED
        assertFails("move") { item.moveTo(StrictCarbonReportEstimateStatus.PLANNED) }
    }
}
