package com.acme.transport.crewing.availability.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingAvailabilityReviewTest {
    @Test
    fun `starts in planned`() {
        val item = newPendingAvailabilityReview("code-193", "label-952")
        assertEquals(PendingAvailabilityReviewStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newPendingAvailabilityReview("code-193", "label-952")
        try {
            item.moveTo(PendingAvailabilityReviewStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingAvailabilityReviewStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPendingAvailabilityReview("code-193", "label-952")
        item.status = PendingAvailabilityReviewStatus.CANCELLED
        assertFails("move") { item.moveTo(PendingAvailabilityReviewStatus.PLANNED) }
    }
}
