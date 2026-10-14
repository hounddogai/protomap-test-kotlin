package com.acme.transport.lastmile.drop.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DropMilestoneTest {
    @Test
    fun `starts in idle`() {
        val item = newDropMilestone("code-131", "name-608")
        assertEquals(DropMilestoneStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newDropMilestone("code-131", "name-608")
        try {
            item.moveTo(DropMilestoneStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DropMilestoneStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDropMilestone("code-131", "name-608")
        item.status = DropMilestoneStatus.STOPPED
        assertFails("move") { item.moveTo(DropMilestoneStatus.IDLE) }
    }
}
