package com.acme.transport.emissions.carbonreport.tieredreportscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedCarbonReportScoreTest {
    @Test
    fun `starts in planned`() {
        val item = newWeightedCarbonReportScore("code-988", "label-130")
        assertEquals(WeightedCarbonReportScoreStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newWeightedCarbonReportScore("code-988", "label-130")
        try {
            item.moveTo(WeightedCarbonReportScoreStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedCarbonReportScoreStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newWeightedCarbonReportScore("code-988", "label-130")
        item.status = WeightedCarbonReportScoreStatus.CANCELLED
        assertFails("move") { item.moveTo(WeightedCarbonReportScoreStatus.PLANNED) }
    }
}
