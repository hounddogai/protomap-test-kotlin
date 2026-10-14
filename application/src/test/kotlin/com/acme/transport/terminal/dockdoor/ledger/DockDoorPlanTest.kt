package com.acme.transport.terminal.dockdoor.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DockDoorPlanTest {
    @Test
    fun `starts in idle`() {
        val item = newDockDoorPlan("code-408", "name-874")
        assertEquals(DockDoorPlanStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newDockDoorPlan("code-408", "name-874")
        try {
            item.moveTo(DockDoorPlanStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DockDoorPlanStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDockDoorPlan("code-408", "name-874")
        item.status = DockDoorPlanStatus.STOPPED
        assertFails("move") { item.moveTo(DockDoorPlanStatus.IDLE) }
    }
}
