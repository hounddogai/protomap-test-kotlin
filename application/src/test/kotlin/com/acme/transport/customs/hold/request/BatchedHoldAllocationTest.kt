package com.acme.transport.customs.hold.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedHoldAllocationTest {
    @Test
    fun `starts in new`() {
        val item = newBatchedHoldAllocation("code-182", "title-361")
        assertEquals(BatchedHoldAllocationStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newBatchedHoldAllocation("code-182", "title-361")
        try {
            item.moveTo(BatchedHoldAllocationStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedHoldAllocationStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newBatchedHoldAllocation("code-182", "title-361")
        item.status = BatchedHoldAllocationStatus.FAILED
        assertFails("move") { item.moveTo(BatchedHoldAllocationStatus.NEW) }
    }
}
