package com.acme.transport.crewing.availability.score

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientAvailabilityReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientAvailabilityReview("code-112", "label-778")
        val view = toLenientAvailabilityReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientAvailabilityReviewViews(mutableListOf<LenientAvailabilityReview>(item, item)).size, "views")
    }
}
