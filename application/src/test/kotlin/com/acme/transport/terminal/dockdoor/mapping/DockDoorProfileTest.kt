package com.acme.transport.terminal.dockdoor.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DockDoorProfileTest {
    @Test
    fun `starts in draft`() {
        val item = newDockDoorProfile("code-712", "label-544")
        assertEquals(DockDoorProfileStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newDockDoorProfile("code-712", "label-544")
        try {
            item.moveTo(DockDoorProfileStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DockDoorProfileStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDockDoorProfile("code-712", "label-544")
        item.status = DockDoorProfileStatus.CLOSED
        assertFails("move") { item.moveTo(DockDoorProfileStatus.DRAFT) }
    }
}
