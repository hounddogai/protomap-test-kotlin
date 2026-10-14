package com.acme.transport.linehaul.trailer.trailerbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedTrailerBatchTest {
    @Test
    fun `starts in proposed`() {
        val item = newBatchedTrailerBatch("code-498", "label-978")
        assertEquals(BatchedTrailerBatchStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newBatchedTrailerBatch("code-498", "label-978")
        try {
            item.moveTo(BatchedTrailerBatchStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedTrailerBatchStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newBatchedTrailerBatch("code-498", "label-978")
        item.status = BatchedTrailerBatchStatus.WITHDRAWN
        assertFails("move") { item.moveTo(BatchedTrailerBatchStatus.PROPOSED) }
    }
}
