package com.acme.transport.terminal.dockdoor.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DockDoorCycleTest {
    @Test
    fun `starts in draft`() {
        val item = newDockDoorCycle("code-388", "title-373")
        assertEquals(DockDoorCycleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newDockDoorCycle("code-388", "title-373")
        try {
            item.moveTo(DockDoorCycleStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DockDoorCycleStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDockDoorCycle("code-388", "title-373")
        item.status = DockDoorCycleStatus.CLOSED
        assertFails("move") { item.moveTo(DockDoorCycleStatus.DRAFT) }
    }
}
