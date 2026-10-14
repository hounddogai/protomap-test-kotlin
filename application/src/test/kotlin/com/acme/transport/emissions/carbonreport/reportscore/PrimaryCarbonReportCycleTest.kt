package com.acme.transport.emissions.carbonreport.reportscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryCarbonReportCycleTest {
    @Test
    fun `starts in proposed`() {
        val item = newPrimaryCarbonReportCycle("code-109", "name-87")
        assertEquals(PrimaryCarbonReportCycleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newPrimaryCarbonReportCycle("code-109", "name-87")
        try {
            item.moveTo(PrimaryCarbonReportCycleStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryCarbonReportCycleStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newPrimaryCarbonReportCycle("code-109", "name-87")
        item.status = PrimaryCarbonReportCycleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(PrimaryCarbonReportCycleStatus.PROPOSED) }
    }
}
