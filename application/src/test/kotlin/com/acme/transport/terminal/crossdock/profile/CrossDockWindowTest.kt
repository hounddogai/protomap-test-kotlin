package com.acme.transport.terminal.crossdock.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CrossDockWindowTest {
    @Test
    fun `starts in idle`() {
        val item = newCrossDockWindow("code-357", "label-132")
        assertEquals(CrossDockWindowStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newCrossDockWindow("code-357", "label-132")
        try {
            item.moveTo(CrossDockWindowStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CrossDockWindowStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCrossDockWindow("code-357", "label-132")
        item.status = CrossDockWindowStatus.STOPPED
        assertFails("move") { item.moveTo(CrossDockWindowStatus.IDLE) }
    }
}
