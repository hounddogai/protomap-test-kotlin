package com.acme.transport.terminal.dockdoor.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DockDoorSlotTest {
    @Test
    fun `starts in proposed`() {
        val item = newDockDoorSlot("code-39", "name-295")
        assertEquals(DockDoorSlotStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newDockDoorSlot("code-39", "name-295")
        try {
            item.moveTo(DockDoorSlotStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DockDoorSlotStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDockDoorSlot("code-39", "name-295")
        item.status = DockDoorSlotStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DockDoorSlotStatus.PROPOSED) }
    }
}
