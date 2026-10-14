package com.acme.transport.terminal.dockdoor.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DockDoorTargetTest {
    @Test
    fun `starts in new`() {
        val item = newDockDoorTarget("code-683", "name-995")
        assertEquals(DockDoorTargetStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newDockDoorTarget("code-683", "name-995")
        try {
            item.moveTo(DockDoorTargetStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DockDoorTargetStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDockDoorTarget("code-683", "name-995")
        item.status = DockDoorTargetStatus.FAILED
        assertFails("move") { item.moveTo(DockDoorTargetStatus.NEW) }
    }
}
