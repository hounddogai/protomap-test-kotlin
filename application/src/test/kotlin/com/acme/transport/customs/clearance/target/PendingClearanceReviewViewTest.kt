package com.acme.transport.customs.clearance.target

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingClearanceReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingClearanceReview("code-814", "title-473")
        val view = toPendingClearanceReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingClearanceReviewViews(mutableListOf<PendingClearanceReview>(item, item)).size, "views")
    }
}
