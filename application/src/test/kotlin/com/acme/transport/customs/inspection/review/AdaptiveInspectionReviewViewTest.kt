package com.acme.transport.customs.inspection.review

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveInspectionReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveInspectionReview("code-15", "name-848")
        val view = toAdaptiveInspectionReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveInspectionReviewViews(mutableListOf<AdaptiveInspectionReview>(item, item)).size, "views")
    }
}
