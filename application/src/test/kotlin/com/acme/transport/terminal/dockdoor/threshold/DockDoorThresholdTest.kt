package com.acme.transport.terminal.dockdoor.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DockDoorThresholdTest {
    @Test
    fun `starts in draft`() {
        val item = newDockDoorThreshold("code-993", "name-14")
        assertEquals(DockDoorThresholdStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newDockDoorThreshold("code-993", "name-14")
        try {
            item.moveTo(DockDoorThresholdStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DockDoorThresholdStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDockDoorThreshold("code-993", "name-14")
        item.status = DockDoorThresholdStatus.CLOSED
        assertFails("move") { item.moveTo(DockDoorThresholdStatus.DRAFT) }
    }
}
