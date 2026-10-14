package com.acme.transport.linehaul.trailer.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryTrailerReviewTest {
    @Test
    fun `starts in idle`() {
        val item = newPrimaryTrailerReview("code-646", "label-181")
        assertEquals(PrimaryTrailerReviewStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newPrimaryTrailerReview("code-646", "label-181")
        try {
            item.moveTo(PrimaryTrailerReviewStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryTrailerReviewStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newPrimaryTrailerReview("code-646", "label-181")
        item.status = PrimaryTrailerReviewStatus.STOPPED
        assertFails("move") { item.moveTo(PrimaryTrailerReviewStatus.IDLE) }
    }
}
