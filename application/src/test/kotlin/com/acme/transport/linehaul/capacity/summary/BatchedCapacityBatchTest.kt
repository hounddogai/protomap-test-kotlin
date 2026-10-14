package com.acme.transport.linehaul.capacity.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedCapacityBatchTest {
    @Test
    fun `starts in idle`() {
        val item = newBatchedCapacityBatch("code-170", "title-598")
        assertEquals(BatchedCapacityBatchStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newBatchedCapacityBatch("code-170", "title-598")
        try {
            item.moveTo(BatchedCapacityBatchStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedCapacityBatchStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newBatchedCapacityBatch("code-170", "title-598")
        item.status = BatchedCapacityBatchStatus.STOPPED
        assertFails("move") { item.moveTo(BatchedCapacityBatchStatus.IDLE) }
    }
}
