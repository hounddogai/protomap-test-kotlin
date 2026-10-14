package com.acme.transport.safety.scorecard.bundle

import kotlin.test.Test
import kotlin.test.assertEquals

class ScorecardReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScorecardReview("code-105", "title-126")
        val view = toScorecardReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScorecardReviewViews(mutableListOf<ScorecardReview>(item, item)).size, "views")
    }
}
