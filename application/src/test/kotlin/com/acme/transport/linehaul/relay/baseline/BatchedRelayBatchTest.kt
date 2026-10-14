package com.acme.transport.linehaul.relay.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedRelayBatchTest {
    @Test
    fun `starts in planned`() {
        val item = newBatchedRelayBatch("code-486", "name-704")
        assertEquals(BatchedRelayBatchStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newBatchedRelayBatch("code-486", "name-704")
        try {
            item.moveTo(BatchedRelayBatchStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedRelayBatchStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newBatchedRelayBatch("code-486", "name-704")
        item.status = BatchedRelayBatchStatus.CANCELLED
        assertFails("move") { item.moveTo(BatchedRelayBatchStatus.PLANNED) }
    }
}
