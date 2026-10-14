package com.acme.transport.fleetops.defect.defectreview

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingDefectReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingDefectReview("code-671", "name-462")
        val view = toPendingDefectReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingDefectReviewViews(mutableListOf<PendingDefectReview>(item, item)).size, "views")
    }
}
