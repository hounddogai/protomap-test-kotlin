package com.acme.transport.customs.inspection.notice

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyInspectionReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyInspectionReview("code-216", "title-126")
        val view = toLegacyInspectionReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyInspectionReviewViews(mutableListOf<LegacyInspectionReview>(item, item)).size, "views")
    }
}
