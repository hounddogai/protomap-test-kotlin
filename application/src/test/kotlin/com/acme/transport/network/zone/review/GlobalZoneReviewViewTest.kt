package com.acme.transport.network.zone.review

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalZoneReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalZoneReview("code-846", "title-116")
        val view = toGlobalZoneReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalZoneReviewViews(mutableListOf<GlobalZoneReview>(item, item)).size, "views")
    }
}
