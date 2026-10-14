package com.acme.transport.linehaul.capacity.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class CapacityReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCapacityReview("code-179", "label-504")
        val view = toCapacityReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCapacityReviewViews(mutableListOf<CapacityReview>(item, item)).size, "views")
    }
}
