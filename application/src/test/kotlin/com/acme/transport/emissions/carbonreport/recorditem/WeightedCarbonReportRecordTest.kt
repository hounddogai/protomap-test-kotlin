package com.acme.transport.emissions.carbonreport.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedCarbonReportRecordTest {
    @Test
    fun `starts in idle`() {
        val item = newWeightedCarbonReportRecord("code-145", "label-404")
        assertEquals(WeightedCarbonReportRecordStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newWeightedCarbonReportRecord("code-145", "label-404")
        try {
            item.moveTo(WeightedCarbonReportRecordStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedCarbonReportRecordStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newWeightedCarbonReportRecord("code-145", "label-404")
        item.status = WeightedCarbonReportRecordStatus.STOPPED
        assertFails("move") { item.moveTo(WeightedCarbonReportRecordStatus.IDLE) }
    }
}
