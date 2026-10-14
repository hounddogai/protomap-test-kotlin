package com.acme.transport.crewing.restbreak.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedRestBreakReviewTest {
    @Test
    fun `starts in idle`() {
        val item = newBatchedRestBreakReview("code-508", "label-777")
        assertEquals(BatchedRestBreakReviewStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newBatchedRestBreakReview("code-508", "label-777")
        try {
            item.moveTo(BatchedRestBreakReviewStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedRestBreakReviewStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newBatchedRestBreakReview("code-508", "label-777")
        item.status = BatchedRestBreakReviewStatus.STOPPED
        assertFails("move") { item.moveTo(BatchedRestBreakReviewStatus.IDLE) }
    }
}
