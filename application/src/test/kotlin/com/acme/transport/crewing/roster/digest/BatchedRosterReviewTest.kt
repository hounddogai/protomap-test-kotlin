package com.acme.transport.crewing.roster.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedRosterReviewTest {
    @Test
    fun `starts in requested`() {
        val item = newBatchedRosterReview("code-66", "name-807")
        assertEquals(BatchedRosterReviewStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newBatchedRosterReview("code-66", "name-807")
        try {
            item.moveTo(BatchedRosterReviewStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedRosterReviewStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newBatchedRosterReview("code-66", "name-807")
        item.status = BatchedRosterReviewStatus.EXPIRED
        assertFails("move") { item.moveTo(BatchedRosterReviewStatus.REQUESTED) }
    }
}
