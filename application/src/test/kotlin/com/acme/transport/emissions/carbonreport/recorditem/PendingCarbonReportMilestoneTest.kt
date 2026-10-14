package com.acme.transport.emissions.carbonreport.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingCarbonReportMilestoneTest {
    @Test
    fun `starts in new`() {
        val item = newPendingCarbonReportMilestone("code-215", "title-741")
        assertEquals(PendingCarbonReportMilestoneStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newPendingCarbonReportMilestone("code-215", "title-741")
        try {
            item.moveTo(PendingCarbonReportMilestoneStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingCarbonReportMilestoneStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newPendingCarbonReportMilestone("code-215", "title-741")
        item.status = PendingCarbonReportMilestoneStatus.FAILED
        assertFails("move") { item.moveTo(PendingCarbonReportMilestoneStatus.NEW) }
    }
}
