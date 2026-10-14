package com.acme.transport.emissions.carbonreport.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredCarbonReportAdjustmentTest {
    @Test
    fun `starts in planned`() {
        val item = newTieredCarbonReportAdjustment("code-867", "label-576")
        assertEquals(TieredCarbonReportAdjustmentStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newTieredCarbonReportAdjustment("code-867", "label-576")
        try {
            item.moveTo(TieredCarbonReportAdjustmentStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredCarbonReportAdjustmentStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTieredCarbonReportAdjustment("code-867", "label-576")
        item.status = TieredCarbonReportAdjustmentStatus.CANCELLED
        assertFails("move") { item.moveTo(TieredCarbonReportAdjustmentStatus.PLANNED) }
    }
}
