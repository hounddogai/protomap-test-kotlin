package com.acme.transport.network.transittime.threshold

import kotlin.test.Test
import kotlin.test.assertEquals

class TransitTimeReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTransitTimeReview("code-526", "label-575")
        val view = toTransitTimeReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTransitTimeReviewViews(mutableListOf<TransitTimeReview>(item, item)).size, "views")
    }
}
