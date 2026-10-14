package com.acme.transport.customs.inspection.target

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionReview("code-216", "name-637")
        val view = toInspectionReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionReviewViews(mutableListOf<InspectionReview>(item, item)).size, "views")
    }
}
