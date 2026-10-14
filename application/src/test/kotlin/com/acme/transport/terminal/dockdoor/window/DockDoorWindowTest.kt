package com.acme.transport.terminal.dockdoor.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DockDoorWindowTest {
    @Test
    fun `starts in open`() {
        val item = newDockDoorWindow("code-581", "name-804")
        assertEquals(DockDoorWindowStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newDockDoorWindow("code-581", "name-804")
        try {
            item.moveTo(DockDoorWindowStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DockDoorWindowStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newDockDoorWindow("code-581", "name-804")
        item.status = DockDoorWindowStatus.ARCHIVED
        assertFails("move") { item.moveTo(DockDoorWindowStatus.OPEN) }
    }
}
