package com.acme.transport.network.zone.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedZoneSegmentTest {
    @Test
    fun `starts in idle`() {
        val item = newBatchedZoneSegment("code-377", "name-511")
        assertEquals(BatchedZoneSegmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newBatchedZoneSegment("code-377", "name-511")
        try {
            item.moveTo(BatchedZoneSegmentStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedZoneSegmentStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newBatchedZoneSegment("code-377", "name-511")
        item.status = BatchedZoneSegmentStatus.STOPPED
        assertFails("move") { item.moveTo(BatchedZoneSegmentStatus.IDLE) }
    }
}
