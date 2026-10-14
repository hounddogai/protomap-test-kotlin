package com.acme.transport.freightpay.dispute.review

import kotlin.test.Test
import kotlin.test.assertEquals

class DisputeReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDisputeReview("code-811", "title-527")
        val view = toDisputeReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDisputeReviewViews(mutableListOf<DisputeReview>(item, item)).size, "views")
    }
}
