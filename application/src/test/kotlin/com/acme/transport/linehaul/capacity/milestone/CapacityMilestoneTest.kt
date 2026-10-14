package com.acme.transport.linehaul.capacity.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CapacityMilestoneTest {
    @Test
    fun `starts in planned`() {
        val item = newCapacityMilestone("code-711", "name-936")
        assertEquals(CapacityMilestoneStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newCapacityMilestone("code-711", "name-936")
        try {
            item.moveTo(CapacityMilestoneStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CapacityMilestoneStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newCapacityMilestone("code-711", "name-936")
        item.status = CapacityMilestoneStatus.CANCELLED
        assertFails("move") { item.moveTo(CapacityMilestoneStatus.PLANNED) }
    }
}
