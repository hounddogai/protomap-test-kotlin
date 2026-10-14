package com.acme.transport.freightpay.dispute.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedDisputeBatchTest {
    @Test
    fun `starts in planned`() {
        val item = newBatchedDisputeBatch("code-409", "label-437")
        assertEquals(BatchedDisputeBatchStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newBatchedDisputeBatch("code-409", "label-437")
        try {
            item.moveTo(BatchedDisputeBatchStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedDisputeBatchStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newBatchedDisputeBatch("code-409", "label-437")
        item.status = BatchedDisputeBatchStatus.CANCELLED
        assertFails("move") { item.moveTo(BatchedDisputeBatchStatus.PLANNED) }
    }
}
