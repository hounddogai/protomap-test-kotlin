package com.acme.transport.terminal.traileryard.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerYardReviewTest {
    @Test
    fun `starts in open`() {
        val item = newTrailerYardReview("code-768", "label-428")
        assertEquals(TrailerYardReviewStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newTrailerYardReview("code-768", "label-428")
        try {
            item.moveTo(TrailerYardReviewStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerYardReviewStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newTrailerYardReview("code-768", "label-428")
        item.status = TrailerYardReviewStatus.ARCHIVED
        assertFails("move") { item.moveTo(TrailerYardReviewStatus.OPEN) }
    }
}
