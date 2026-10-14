package com.acme.transport.fleetops.utilization.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictUtilizationReviewTest {
    @Test
    fun `starts in planned`() {
        val item = newStrictUtilizationReview("code-498", "title-236")
        assertEquals(StrictUtilizationReviewStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newStrictUtilizationReview("code-498", "title-236")
        try {
            item.moveTo(StrictUtilizationReviewStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictUtilizationReviewStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newStrictUtilizationReview("code-498", "title-236")
        item.status = StrictUtilizationReviewStatus.CANCELLED
        assertFails("move") { item.moveTo(StrictUtilizationReviewStatus.PLANNED) }
    }
}
