package com.acme.transport.emissions.carbonreport.strictreportadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryCarbonReportAdjustmentTest {
    @Test
    fun `starts in requested`() {
        val item = newPrimaryCarbonReportAdjustment("code-725", "title-532")
        assertEquals(PrimaryCarbonReportAdjustmentStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newPrimaryCarbonReportAdjustment("code-725", "title-532")
        try {
            item.moveTo(PrimaryCarbonReportAdjustmentStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryCarbonReportAdjustmentStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPrimaryCarbonReportAdjustment("code-725", "title-532")
        item.status = PrimaryCarbonReportAdjustmentStatus.EXPIRED
        assertFails("move") { item.moveTo(PrimaryCarbonReportAdjustmentStatus.REQUESTED) }
    }
}
