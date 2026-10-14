package com.acme.transport.network.hub.audit

import kotlin.test.Test
import kotlin.test.assertEquals

class HubReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newHubReview("code-261", "title-27")
        val view = toHubReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toHubReviewViews(mutableListOf<HubReview>(item, item)).size, "views")
    }
}
