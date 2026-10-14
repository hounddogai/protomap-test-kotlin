package com.acme.transport.fleetops.serviceplan.estimate

import kotlin.test.Test
import kotlin.test.assertEquals

class ServicePlanEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newServicePlanEstimate("code-791", "title-88")
        val view = toServicePlanEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toServicePlanEstimateViews(mutableListOf<ServicePlanEstimate>(item, item)).size, "views")
    }
}
