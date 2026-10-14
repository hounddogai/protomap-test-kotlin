package com.acme.transport.emissions.evcharge.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedEvChargeSegmentTest {
    @Test
    fun `starts in idle`() {
        val item = newBatchedEvChargeSegment("code-779", "label-223")
        assertEquals(BatchedEvChargeSegmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newBatchedEvChargeSegment("code-779", "label-223")
        try {
            item.moveTo(BatchedEvChargeSegmentStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedEvChargeSegmentStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newBatchedEvChargeSegment("code-779", "label-223")
        item.status = BatchedEvChargeSegmentStatus.STOPPED
        assertFails("move") { item.moveTo(BatchedEvChargeSegmentStatus.IDLE) }
    }
}
