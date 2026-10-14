package com.acme.transport.terminal.crossdock.dockslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CrossDockAdjustmentTest {
    @Test
    fun `starts in open`() {
        val item = newCrossDockAdjustment("code-226", "name-720")
        assertEquals(CrossDockAdjustmentStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newCrossDockAdjustment("code-226", "name-720")
        try {
            item.moveTo(CrossDockAdjustmentStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CrossDockAdjustmentStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCrossDockAdjustment("code-226", "name-720")
        item.status = CrossDockAdjustmentStatus.ARCHIVED
        assertFails("move") { item.moveTo(CrossDockAdjustmentStatus.OPEN) }
    }
}
