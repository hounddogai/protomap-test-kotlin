package com.acme.transport.terminal.crossdock.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CrossDockAllocationTest {
    @Test
    fun `starts in requested`() {
        val item = newCrossDockAllocation("code-345", "title-403")
        assertEquals(CrossDockAllocationStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newCrossDockAllocation("code-345", "title-403")
        try {
            item.moveTo(CrossDockAllocationStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CrossDockAllocationStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCrossDockAllocation("code-345", "title-403")
        item.status = CrossDockAllocationStatus.EXPIRED
        assertFails("move") { item.moveTo(CrossDockAllocationStatus.REQUESTED) }
    }
}
