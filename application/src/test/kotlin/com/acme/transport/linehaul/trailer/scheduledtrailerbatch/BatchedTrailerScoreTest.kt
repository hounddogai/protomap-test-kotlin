package com.acme.transport.linehaul.trailer.scheduledtrailerbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedTrailerScoreTest {
    @Test
    fun `starts in idle`() {
        val item = newBatchedTrailerScore("code-957", "label-279")
        assertEquals(BatchedTrailerScoreStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newBatchedTrailerScore("code-957", "label-279")
        try {
            item.moveTo(BatchedTrailerScoreStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedTrailerScoreStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newBatchedTrailerScore("code-957", "label-279")
        item.status = BatchedTrailerScoreStatus.STOPPED
        assertFails("move") { item.moveTo(BatchedTrailerScoreStatus.IDLE) }
    }
}
