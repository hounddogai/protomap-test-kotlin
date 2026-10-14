package com.acme.transport.terminal.crossdock.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CrossDockSlotTest {
    @Test
    fun `starts in new`() {
        val item = newCrossDockSlot("code-595", "label-999")
        assertEquals(CrossDockSlotStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newCrossDockSlot("code-595", "label-999")
        try {
            item.moveTo(CrossDockSlotStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CrossDockSlotStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCrossDockSlot("code-595", "label-999")
        item.status = CrossDockSlotStatus.FAILED
        assertFails("move") { item.moveTo(CrossDockSlotStatus.NEW) }
    }
}
