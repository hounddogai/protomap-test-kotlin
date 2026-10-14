package com.acme.transport.fleetops.serviceplan.threshold

import kotlin.test.Test
import kotlin.test.assertEquals

class ServicePlanThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newServicePlanThreshold("code-919", "title-373")
        val view = toServicePlanThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toServicePlanThresholdViews(mutableListOf<ServicePlanThreshold>(item, item)).size, "views")
    }
}
