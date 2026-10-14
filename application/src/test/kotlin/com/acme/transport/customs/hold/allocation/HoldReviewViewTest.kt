package com.acme.transport.customs.hold.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class HoldReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newHoldReview("code-453", "title-339")
        val view = toHoldReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toHoldReviewViews(mutableListOf<HoldReview>(item, item)).size, "views")
    }
}
