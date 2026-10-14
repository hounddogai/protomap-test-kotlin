package com.acme.transport.emissions.carbonreport.reportmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalCarbonReportAdjustmentTest {
    @Test
    fun `starts in created`() {
        val item = newGlobalCarbonReportAdjustment("code-211", "name-615")
        assertEquals(GlobalCarbonReportAdjustmentStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newGlobalCarbonReportAdjustment("code-211", "name-615")
        try {
            item.moveTo(GlobalCarbonReportAdjustmentStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalCarbonReportAdjustmentStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newGlobalCarbonReportAdjustment("code-211", "name-615")
        item.status = GlobalCarbonReportAdjustmentStatus.RETIRED
        assertFails("move") { item.moveTo(GlobalCarbonReportAdjustmentStatus.CREATED) }
    }
}
