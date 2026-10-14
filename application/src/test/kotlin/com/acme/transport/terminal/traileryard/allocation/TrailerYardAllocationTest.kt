package com.acme.transport.terminal.traileryard.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerYardAllocationTest {
    @Test
    fun `starts in idle`() {
        val item = newTrailerYardAllocation("code-40", "label-721")
        assertEquals(TrailerYardAllocationStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newTrailerYardAllocation("code-40", "label-721")
        try {
            item.moveTo(TrailerYardAllocationStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerYardAllocationStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTrailerYardAllocation("code-40", "label-721")
        item.status = TrailerYardAllocationStatus.STOPPED
        assertFails("move") { item.moveTo(TrailerYardAllocationStatus.IDLE) }
    }
}
