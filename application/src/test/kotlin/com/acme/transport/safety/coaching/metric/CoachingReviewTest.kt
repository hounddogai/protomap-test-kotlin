package com.acme.transport.safety.coaching.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CoachingReviewTest {
    @Test
    fun `starts in planned`() {
        val item = newCoachingReview("code-716", "title-846")
        assertEquals(CoachingReviewStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newCoachingReview("code-716", "title-846")
        try {
            item.moveTo(CoachingReviewStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CoachingReviewStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newCoachingReview("code-716", "title-846")
        item.status = CoachingReviewStatus.CANCELLED
        assertFails("move") { item.moveTo(CoachingReviewStatus.PLANNED) }
    }
}
