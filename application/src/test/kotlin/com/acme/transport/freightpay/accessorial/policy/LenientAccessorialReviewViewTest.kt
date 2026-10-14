package com.acme.transport.freightpay.accessorial.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientAccessorialReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientAccessorialReview("code-584", "name-434")
        val view = toLenientAccessorialReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientAccessorialReviewViews(mutableListOf<LenientAccessorialReview>(item, item)).size, "views")
    }
}
