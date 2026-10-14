package com.acme.transport.fleetops.utilization.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledUtilizationMilestoneTest {
    @Test
    fun `starts in created`() {
        val item = newScheduledUtilizationMilestone("code-997", "name-630")
        assertEquals(ScheduledUtilizationMilestoneStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newScheduledUtilizationMilestone("code-997", "name-630")
        try {
            item.moveTo(ScheduledUtilizationMilestoneStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledUtilizationMilestoneStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newScheduledUtilizationMilestone("code-997", "name-630")
        item.status = ScheduledUtilizationMilestoneStatus.RETIRED
        assertFails("move") { item.moveTo(ScheduledUtilizationMilestoneStatus.CREATED) }
    }
}
