package com.acme.transport.fleetops.defect.review

import kotlin.test.Test
import kotlin.test.assertEquals

class DefectReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefectReview("code-706", "name-60")
        val view = toDefectReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefectReviewViews(mutableListOf<DefectReview>(item, item)).size, "views")
    }
}
