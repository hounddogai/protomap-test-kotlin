package com.acme.transport.fleetops.inspection.review

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionReview("code-114", "title-635")
        val view = toInspectionReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionReviewViews(mutableListOf<InspectionReview>(item, item)).size, "views")
    }
}
