package com.acme.transport.customs.clearance.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingClearanceAllocationTest {
    @Test
    fun `starts in requested`() {
        val item = newPendingClearanceAllocation("code-762", "title-684")
        assertEquals(PendingClearanceAllocationStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newPendingClearanceAllocation("code-762", "title-684")
        try {
            item.moveTo(PendingClearanceAllocationStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingClearanceAllocationStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPendingClearanceAllocation("code-762", "title-684")
        item.status = PendingClearanceAllocationStatus.EXPIRED
        assertFails("move") { item.moveTo(PendingClearanceAllocationStatus.REQUESTED) }
    }
}
