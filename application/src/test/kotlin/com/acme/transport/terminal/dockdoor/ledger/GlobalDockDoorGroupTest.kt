package com.acme.transport.terminal.dockdoor.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalDockDoorGroupTest {
    @Test
    fun `starts in open`() {
        val item = newGlobalDockDoorGroup("code-686", "name-193")
        assertEquals(GlobalDockDoorGroupStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newGlobalDockDoorGroup("code-686", "name-193")
        try {
            item.moveTo(GlobalDockDoorGroupStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalDockDoorGroupStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newGlobalDockDoorGroup("code-686", "name-193")
        item.status = GlobalDockDoorGroupStatus.ARCHIVED
        assertFails("move") { item.moveTo(GlobalDockDoorGroupStatus.OPEN) }
    }
}
