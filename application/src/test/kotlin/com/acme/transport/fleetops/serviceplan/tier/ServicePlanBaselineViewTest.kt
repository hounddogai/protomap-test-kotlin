package com.acme.transport.fleetops.serviceplan.tier

import kotlin.test.Test
import kotlin.test.assertEquals

class ServicePlanBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newServicePlanBaseline("code-451", "label-848")
        val view = toServicePlanBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toServicePlanBaselineViews(mutableListOf<ServicePlanBaseline>(item, item)).size, "views")
    }
}
