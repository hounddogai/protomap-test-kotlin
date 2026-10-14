package com.acme.transport.linehaul.trailer.review

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryTrailerReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryTrailerReview("code-646", "label-181")
        val view = toPrimaryTrailerReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryTrailerReviewViews(mutableListOf<PrimaryTrailerReview>(item, item)).size, "views")
    }
}
