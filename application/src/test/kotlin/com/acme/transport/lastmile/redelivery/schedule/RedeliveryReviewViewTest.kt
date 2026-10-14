package com.acme.transport.lastmile.redelivery.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class RedeliveryReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRedeliveryReview("code-177", "title-72")
        val view = toRedeliveryReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRedeliveryReviewViews(mutableListOf<RedeliveryReview>(item, item)).size, "views")
    }
}
