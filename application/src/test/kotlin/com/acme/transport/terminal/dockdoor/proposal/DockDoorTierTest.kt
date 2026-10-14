package com.acme.transport.terminal.dockdoor.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DockDoorTierTest {
    @Test
    fun `starts in idle`() {
        val item = newDockDoorTier("code-141", "name-959")
        assertEquals(DockDoorTierStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newDockDoorTier("code-141", "name-959")
        try {
            item.moveTo(DockDoorTierStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DockDoorTierStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDockDoorTier("code-141", "name-959")
        item.status = DockDoorTierStatus.STOPPED
        assertFails("move") { item.moveTo(DockDoorTierStatus.IDLE) }
    }
}
