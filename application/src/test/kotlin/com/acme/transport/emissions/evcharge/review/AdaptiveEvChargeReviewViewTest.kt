package com.acme.transport.emissions.evcharge.review

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveEvChargeReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveEvChargeReview("code-348", "label-201")
        val view = toAdaptiveEvChargeReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveEvChargeReviewViews(mutableListOf<AdaptiveEvChargeReview>(item, item)).size, "views")
    }
}
