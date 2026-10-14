package com.acme.transport.crewing.availability.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingAvailabilityReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingAvailabilityReview("code-193", "label-952")
        val view = toPendingAvailabilityReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingAvailabilityReviewViews(mutableListOf<PendingAvailabilityReview>(item, item)).size, "views")
    }
}
