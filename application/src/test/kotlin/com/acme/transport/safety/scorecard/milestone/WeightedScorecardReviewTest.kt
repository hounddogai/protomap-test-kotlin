package com.acme.transport.safety.scorecard.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedScorecardReviewTest {
    @Test
    fun `starts in draft`() {
        val item = newWeightedScorecardReview("code-649", "name-845")
        assertEquals(WeightedScorecardReviewStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newWeightedScorecardReview("code-649", "name-845")
        try {
            item.moveTo(WeightedScorecardReviewStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedScorecardReviewStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newWeightedScorecardReview("code-649", "name-845")
        item.status = WeightedScorecardReviewStatus.CLOSED
        assertFails("move") { item.moveTo(WeightedScorecardReviewStatus.DRAFT) }
    }
}
