package com.acme.transport.safety.nearmiss.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedNearMissSegmentTest {
    @Test
    fun `starts in new`() {
        val item = newBatchedNearMissSegment("code-267", "name-703")
        assertEquals(BatchedNearMissSegmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newBatchedNearMissSegment("code-267", "name-703")
        try {
            item.moveTo(BatchedNearMissSegmentStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedNearMissSegmentStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newBatchedNearMissSegment("code-267", "name-703")
        item.status = BatchedNearMissSegmentStatus.FAILED
        assertFails("move") { item.moveTo(BatchedNearMissSegmentStatus.NEW) }
    }
}
