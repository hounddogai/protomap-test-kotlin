package com.acme.transport.terminal.dockdoor.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DockDoorMilestoneTest {
    @Test
    fun `starts in proposed`() {
        val item = newDockDoorMilestone("code-733", "title-881")
        assertEquals(DockDoorMilestoneStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newDockDoorMilestone("code-733", "title-881")
        try {
            item.moveTo(DockDoorMilestoneStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DockDoorMilestoneStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDockDoorMilestone("code-733", "title-881")
        item.status = DockDoorMilestoneStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DockDoorMilestoneStatus.PROPOSED) }
    }
}
