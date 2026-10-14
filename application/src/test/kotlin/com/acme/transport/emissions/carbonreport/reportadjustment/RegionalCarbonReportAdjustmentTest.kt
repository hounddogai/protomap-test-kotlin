package com.acme.transport.emissions.carbonreport.reportadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalCarbonReportAdjustmentTest {
    @Test
    fun `starts in new`() {
        val item = newRegionalCarbonReportAdjustment("code-529", "name-122")
        assertEquals(RegionalCarbonReportAdjustmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newRegionalCarbonReportAdjustment("code-529", "name-122")
        try {
            item.moveTo(RegionalCarbonReportAdjustmentStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalCarbonReportAdjustmentStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRegionalCarbonReportAdjustment("code-529", "name-122")
        item.status = RegionalCarbonReportAdjustmentStatus.FAILED
        assertFails("move") { item.moveTo(RegionalCarbonReportAdjustmentStatus.NEW) }
    }
}
