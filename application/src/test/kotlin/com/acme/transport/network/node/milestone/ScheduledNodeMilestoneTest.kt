package com.acme.transport.network.node.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledNodeMilestoneTest {
    @Test
    fun `starts in created`() {
        val item = newScheduledNodeMilestone("code-260", "title-40")
        assertEquals(ScheduledNodeMilestoneStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newScheduledNodeMilestone("code-260", "title-40")
        try {
            item.moveTo(ScheduledNodeMilestoneStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledNodeMilestoneStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newScheduledNodeMilestone("code-260", "title-40")
        item.status = ScheduledNodeMilestoneStatus.RETIRED
        assertFails("move") { item.moveTo(ScheduledNodeMilestoneStatus.CREATED) }
    }
}
