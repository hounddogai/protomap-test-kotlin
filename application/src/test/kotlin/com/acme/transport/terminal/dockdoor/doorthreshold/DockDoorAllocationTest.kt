package com.acme.transport.terminal.dockdoor.doorthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DockDoorAllocationTest {
    @Test
    fun `starts in idle`() {
        val item = newDockDoorAllocation("code-440", "title-938")
        assertEquals(DockDoorAllocationStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newDockDoorAllocation("code-440", "title-938")
        try {
            item.moveTo(DockDoorAllocationStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DockDoorAllocationStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDockDoorAllocation("code-440", "title-938")
        item.status = DockDoorAllocationStatus.STOPPED
        assertFails("move") { item.moveTo(DockDoorAllocationStatus.IDLE) }
    }
}
