package com.acme.transport.lastmile.attempt.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalAttemptReviewTest {
    @Test
    fun `starts in requested`() {
        val item = newGlobalAttemptReview("code-316", "name-989")
        assertEquals(GlobalAttemptReviewStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newGlobalAttemptReview("code-316", "name-989")
        try {
            item.moveTo(GlobalAttemptReviewStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalAttemptReviewStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newGlobalAttemptReview("code-316", "name-989")
        item.status = GlobalAttemptReviewStatus.EXPIRED
        assertFails("move") { item.moveTo(GlobalAttemptReviewStatus.REQUESTED) }
    }
}
