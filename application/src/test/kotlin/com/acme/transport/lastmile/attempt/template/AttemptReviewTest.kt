package com.acme.transport.lastmile.attempt.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AttemptReviewTest {
    @Test
    fun `starts in new`() {
        val item = newAttemptReview("code-831", "name-535")
        assertEquals(AttemptReviewStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newAttemptReview("code-831", "name-535")
        try {
            item.moveTo(AttemptReviewStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AttemptReviewStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newAttemptReview("code-831", "name-535")
        item.status = AttemptReviewStatus.FAILED
        assertFails("move") { item.moveTo(AttemptReviewStatus.NEW) }
    }
}
