package com.acme.transport.linehaul.relay.quota

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveRelayReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveRelayReview("code-535", "label-31")
        val view = toAdaptiveRelayReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveRelayReviewViews(mutableListOf<AdaptiveRelayReview>(item, item)).size, "views")
    }
}
