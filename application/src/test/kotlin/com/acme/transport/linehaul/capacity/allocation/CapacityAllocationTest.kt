package com.acme.transport.linehaul.capacity.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CapacityAllocationTest {
    @Test
    fun `starts in idle`() {
        val item = newCapacityAllocation("code-228", "label-191")
        assertEquals(CapacityAllocationStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newCapacityAllocation("code-228", "label-191")
        try {
            item.moveTo(CapacityAllocationStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CapacityAllocationStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCapacityAllocation("code-228", "label-191")
        item.status = CapacityAllocationStatus.STOPPED
        assertFails("move") { item.moveTo(CapacityAllocationStatus.IDLE) }
    }
}
