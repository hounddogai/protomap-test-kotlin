package com.acme.transport.network.transittime.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TransitTimeReviewTest {
    @Test
    fun `starts in idle`() {
        val item = newTransitTimeReview("code-526", "label-575")
        assertEquals(TransitTimeReviewStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newTransitTimeReview("code-526", "label-575")
        try {
            item.moveTo(TransitTimeReviewStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TransitTimeReviewStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTransitTimeReview("code-526", "label-575")
        item.status = TransitTimeReviewStatus.STOPPED
        assertFails("move") { item.moveTo(TransitTimeReviewStatus.IDLE) }
    }
}
