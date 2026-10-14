package com.acme.transport.emissions.evcharge.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveEvChargeReviewTest {
    @Test
    fun `starts in planned`() {
        val item = newAdaptiveEvChargeReview("code-348", "label-201")
        assertEquals(AdaptiveEvChargeReviewStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newAdaptiveEvChargeReview("code-348", "label-201")
        try {
            item.moveTo(AdaptiveEvChargeReviewStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveEvChargeReviewStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAdaptiveEvChargeReview("code-348", "label-201")
        item.status = AdaptiveEvChargeReviewStatus.CANCELLED
        assertFails("move") { item.moveTo(AdaptiveEvChargeReviewStatus.PLANNED) }
    }
}
