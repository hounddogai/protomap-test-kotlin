package com.acme.transport.emissions.carbonreport.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledCarbonReportCycleTest {
    @Test
    fun `starts in draft`() {
        val item = newScheduledCarbonReportCycle("code-146", "title-183")
        assertEquals(ScheduledCarbonReportCycleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newScheduledCarbonReportCycle("code-146", "title-183")
        try {
            item.moveTo(ScheduledCarbonReportCycleStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledCarbonReportCycleStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newScheduledCarbonReportCycle("code-146", "title-183")
        item.status = ScheduledCarbonReportCycleStatus.CLOSED
        assertFails("move") { item.moveTo(ScheduledCarbonReportCycleStatus.DRAFT) }
    }
}
