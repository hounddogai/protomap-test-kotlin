package com.acme.transport.network.servicearea.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class ServiceAreaReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newServiceAreaReview("code-640", "title-778")
        val view = toServiceAreaReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toServiceAreaReviewViews(mutableListOf<ServiceAreaReview>(item, item)).size, "views")
    }
}
