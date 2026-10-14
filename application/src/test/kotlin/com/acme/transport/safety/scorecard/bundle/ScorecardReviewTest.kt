package com.acme.transport.safety.scorecard.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScorecardReviewTest {
    @Test
    fun `starts in requested`() {
        val item = newScorecardReview("code-105", "title-126")
        assertEquals(ScorecardReviewStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newScorecardReview("code-105", "title-126")
        try {
            item.moveTo(ScorecardReviewStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScorecardReviewStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newScorecardReview("code-105", "title-126")
        item.status = ScorecardReviewStatus.EXPIRED
        assertFails("move") { item.moveTo(ScorecardReviewStatus.REQUESTED) }
    }
}
