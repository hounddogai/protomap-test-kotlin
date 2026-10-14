package com.acme.transport.lastmile.parcel.review

import kotlin.test.Test
import kotlin.test.assertEquals

class ParcelReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newParcelReview("code-667", "title-102")
        val view = toParcelReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toParcelReviewViews(mutableListOf<ParcelReview>(item, item)).size, "views")
    }
}
