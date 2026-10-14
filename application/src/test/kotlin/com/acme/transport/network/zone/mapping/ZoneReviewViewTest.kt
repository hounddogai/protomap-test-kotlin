package com.acme.transport.network.zone.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class ZoneReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newZoneReview("code-188", "name-645")
        val view = toZoneReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toZoneReviewViews(mutableListOf<ZoneReview>(item, item)).size, "views")
    }
}
