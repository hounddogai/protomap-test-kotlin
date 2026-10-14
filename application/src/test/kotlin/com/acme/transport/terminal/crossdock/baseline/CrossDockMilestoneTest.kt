package com.acme.transport.terminal.crossdock.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CrossDockMilestoneTest {
    @Test
    fun `starts in idle`() {
        val item = newCrossDockMilestone("code-917", "label-11")
        assertEquals(CrossDockMilestoneStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newCrossDockMilestone("code-917", "label-11")
        try {
            item.moveTo(CrossDockMilestoneStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CrossDockMilestoneStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCrossDockMilestone("code-917", "label-11")
        item.status = CrossDockMilestoneStatus.STOPPED
        assertFails("move") { item.moveTo(CrossDockMilestoneStatus.IDLE) }
    }
}
