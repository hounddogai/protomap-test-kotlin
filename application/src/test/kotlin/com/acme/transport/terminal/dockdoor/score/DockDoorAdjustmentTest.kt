package com.acme.transport.terminal.dockdoor.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DockDoorAdjustmentTest {
    @Test
    fun `starts in planned`() {
        val item = newDockDoorAdjustment("code-389", "title-478")
        assertEquals(DockDoorAdjustmentStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newDockDoorAdjustment("code-389", "title-478")
        try {
            item.moveTo(DockDoorAdjustmentStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DockDoorAdjustmentStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDockDoorAdjustment("code-389", "title-478")
        item.status = DockDoorAdjustmentStatus.CANCELLED
        assertFails("move") { item.moveTo(DockDoorAdjustmentStatus.PLANNED) }
    }
}
