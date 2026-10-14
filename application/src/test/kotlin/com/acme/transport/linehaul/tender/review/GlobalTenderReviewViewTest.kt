package com.acme.transport.linehaul.tender.review

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalTenderReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalTenderReview("code-393", "title-911")
        val view = toGlobalTenderReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalTenderReviewViews(mutableListOf<GlobalTenderReview>(item, item)).size, "views")
    }
}
