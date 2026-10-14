package com.acme.transport.linehaul.laneplan.planbundle

import kotlin.test.Test
import kotlin.test.assertEquals

class LanePlanReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLanePlanReview("code-77", "label-643")
        val view = toLanePlanReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLanePlanReviewViews(mutableListOf<LanePlanReview>(item, item)).size, "views")
    }
}
