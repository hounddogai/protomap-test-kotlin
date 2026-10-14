package com.acme.transport.fleetops.serviceplan.target

import kotlin.test.Test
import kotlin.test.assertEquals

class ServicePlanTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newServicePlanTarget("code-302", "label-365")
        val view = toServicePlanTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toServicePlanTargetViews(mutableListOf<ServicePlanTarget>(item, item)).size, "views")
    }
}
