package com.acme.transport.linehaul.capacity.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientCapacityMilestoneTest {
    @Test
    fun `starts in created`() {
        val item = newLenientCapacityMilestone("code-362", "label-574")
        assertEquals(LenientCapacityMilestoneStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newLenientCapacityMilestone("code-362", "label-574")
        try {
            item.moveTo(LenientCapacityMilestoneStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientCapacityMilestoneStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLenientCapacityMilestone("code-362", "label-574")
        item.status = LenientCapacityMilestoneStatus.RETIRED
        assertFails("move") { item.moveTo(LenientCapacityMilestoneStatus.CREATED) }
    }
}
