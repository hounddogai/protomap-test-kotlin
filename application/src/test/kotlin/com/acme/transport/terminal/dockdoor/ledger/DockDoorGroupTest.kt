package com.acme.transport.terminal.dockdoor.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DockDoorGroupTest {
    @Test
    fun `starts in requested`() {
        val item = newDockDoorGroup("code-871", "label-506")
        assertEquals(DockDoorGroupStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newDockDoorGroup("code-871", "label-506")
        try {
            item.moveTo(DockDoorGroupStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DockDoorGroupStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDockDoorGroup("code-871", "label-506")
        item.status = DockDoorGroupStatus.EXPIRED
        assertFails("move") { item.moveTo(DockDoorGroupStatus.REQUESTED) }
    }
}
