package com.acme.transport.linehaul.trailer.fallbacktrailermilestone

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailerPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTrailerPlan("code-915", "title-667")
        val view = toTrailerPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTrailerPlanViews(mutableListOf<TrailerPlan>(item, item)).size, "views")
    }
}
