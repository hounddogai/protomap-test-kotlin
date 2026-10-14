package com.acme.transport.fleetops.serviceplan.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackServicePlanRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackServicePlanRequest("code-519", "name-490")
        val view = toFallbackServicePlanRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackServicePlanRequestViews(mutableListOf<FallbackServicePlanRequest>(item, item)).size, "views")
    }
}
