package com.acme.transport.lastmile.redelivery.redeliverywindow

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredRedeliveryReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredRedeliveryReview("code-873", "label-749")
        val view = toTieredRedeliveryReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredRedeliveryReviewViews(mutableListOf<TieredRedeliveryReview>(item, item)).size, "views")
    }
}
