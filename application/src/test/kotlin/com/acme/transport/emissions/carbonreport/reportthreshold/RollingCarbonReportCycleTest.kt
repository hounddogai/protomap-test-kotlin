package com.acme.transport.emissions.carbonreport.reportthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingCarbonReportCycleTest {
    @Test
    fun `starts in draft`() {
        val item = newRollingCarbonReportCycle("code-897", "name-894")
        assertEquals(RollingCarbonReportCycleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newRollingCarbonReportCycle("code-897", "name-894")
        try {
            item.moveTo(RollingCarbonReportCycleStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingCarbonReportCycleStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRollingCarbonReportCycle("code-897", "name-894")
        item.status = RollingCarbonReportCycleStatus.CLOSED
        assertFails("move") { item.moveTo(RollingCarbonReportCycleStatus.DRAFT) }
    }
}
