package com.acme.transport.linehaul.trailer.trailerrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedTrailerEntryTest {
    @Test
    fun `starts in idle`() {
        val item = newBatchedTrailerEntry("code-709", "label-102")
        assertEquals(BatchedTrailerEntryStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newBatchedTrailerEntry("code-709", "label-102")
        try {
            item.moveTo(BatchedTrailerEntryStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedTrailerEntryStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newBatchedTrailerEntry("code-709", "label-102")
        item.status = BatchedTrailerEntryStatus.STOPPED
        assertFails("move") { item.moveTo(BatchedTrailerEntryStatus.IDLE) }
    }
}
