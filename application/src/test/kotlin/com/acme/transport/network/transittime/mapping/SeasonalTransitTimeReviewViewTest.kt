package com.acme.transport.network.transittime.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalTransitTimeReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalTransitTimeReview("code-656", "title-552")
        val view = toSeasonalTransitTimeReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalTransitTimeReviewViews(mutableListOf<SeasonalTransitTimeReview>(item, item)).size, "views")
    }
}
