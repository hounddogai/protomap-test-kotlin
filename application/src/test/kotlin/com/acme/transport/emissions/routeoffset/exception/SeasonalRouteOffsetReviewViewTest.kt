package com.acme.transport.emissions.routeoffset.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalRouteOffsetReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalRouteOffsetReview("code-113", "label-397")
        val view = toSeasonalRouteOffsetReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalRouteOffsetReviewViews(mutableListOf<SeasonalRouteOffsetReview>(item, item)).size, "views")
    }
}
