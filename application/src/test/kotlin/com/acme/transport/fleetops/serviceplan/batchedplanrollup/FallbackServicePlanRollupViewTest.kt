package com.acme.transport.fleetops.serviceplan.batchedplanrollup

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackServicePlanRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackServicePlanRollup("code-232", "label-883")
        val view = toFallbackServicePlanRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackServicePlanRollupViews(mutableListOf<FallbackServicePlanRollup>(item, item)).size, "views")
    }
}
