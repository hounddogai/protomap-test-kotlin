package com.acme.transport.linehaul.capacity.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedCapacitySegmentTest {
    @Test
    fun `starts in idle`() {
        val item = newBatchedCapacitySegment("code-119", "name-656")
        assertEquals(BatchedCapacitySegmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newBatchedCapacitySegment("code-119", "name-656")
        try {
            item.moveTo(BatchedCapacitySegmentStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedCapacitySegmentStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newBatchedCapacitySegment("code-119", "name-656")
        item.status = BatchedCapacitySegmentStatus.STOPPED
        assertFails("move") { item.moveTo(BatchedCapacitySegmentStatus.IDLE) }
    }
}
