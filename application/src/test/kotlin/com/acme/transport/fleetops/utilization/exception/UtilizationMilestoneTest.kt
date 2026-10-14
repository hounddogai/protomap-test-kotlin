package com.acme.transport.fleetops.utilization.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class UtilizationMilestoneTest {
    @Test
    fun `starts in planned`() {
        val item = newUtilizationMilestone("code-318", "label-224")
        assertEquals(UtilizationMilestoneStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newUtilizationMilestone("code-318", "label-224")
        try {
            item.moveTo(UtilizationMilestoneStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(UtilizationMilestoneStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newUtilizationMilestone("code-318", "label-224")
        item.status = UtilizationMilestoneStatus.CANCELLED
        assertFails("move") { item.moveTo(UtilizationMilestoneStatus.PLANNED) }
    }
}
