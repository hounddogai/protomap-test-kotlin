package com.acme.transport.safety.scorecard.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedScorecardReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedScorecardReview("code-649", "name-845")
        val view = toWeightedScorecardReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedScorecardReviewViews(mutableListOf<WeightedScorecardReview>(item, item)).size, "views")
    }
}
