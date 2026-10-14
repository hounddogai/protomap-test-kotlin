package com.acme.transport.emissions.carbonreport.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedCarbonReportCycleTest {
    @Test
    fun `starts in proposed`() {
        val item = newCappedCarbonReportCycle("code-721", "name-101")
        assertEquals(CappedCarbonReportCycleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newCappedCarbonReportCycle("code-721", "name-101")
        try {
            item.moveTo(CappedCarbonReportCycleStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedCarbonReportCycleStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newCappedCarbonReportCycle("code-721", "name-101")
        item.status = CappedCarbonReportCycleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(CappedCarbonReportCycleStatus.PROPOSED) }
    }
}
