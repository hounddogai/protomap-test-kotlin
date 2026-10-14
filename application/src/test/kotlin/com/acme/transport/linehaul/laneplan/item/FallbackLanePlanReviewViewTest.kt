package com.acme.transport.linehaul.laneplan.item

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackLanePlanReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackLanePlanReview("code-948", "label-304")
        val view = toFallbackLanePlanReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackLanePlanReviewViews(mutableListOf<FallbackLanePlanReview>(item, item)).size, "views")
    }
}
