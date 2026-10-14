package com.acme.transport.lastmile.parcel.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalParcelReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalParcelReview("code-162", "name-857")
        val view = toRegionalParcelReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalParcelReviewViews(mutableListOf<RegionalParcelReview>(item, item)).size, "views")
    }
}
