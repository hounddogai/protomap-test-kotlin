package com.acme.transport.customs.clearance.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedClearanceAllocationTest {
    @Test
    fun `starts in new`() {
        val item = newBatchedClearanceAllocation("code-22", "title-221")
        assertEquals(BatchedClearanceAllocationStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newBatchedClearanceAllocation("code-22", "title-221")
        try {
            item.moveTo(BatchedClearanceAllocationStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedClearanceAllocationStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newBatchedClearanceAllocation("code-22", "title-221")
        item.status = BatchedClearanceAllocationStatus.FAILED
        assertFails("move") { item.moveTo(BatchedClearanceAllocationStatus.NEW) }
    }
}
