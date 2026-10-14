package com.acme.transport.network.node.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveNodeReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveNodeReview("code-220", "name-728")
        val view = toAdaptiveNodeReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveNodeReviewViews(mutableListOf<AdaptiveNodeReview>(item, item)).size, "views")
    }
}
