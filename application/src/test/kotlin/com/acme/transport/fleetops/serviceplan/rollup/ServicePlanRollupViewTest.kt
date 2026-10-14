package com.acme.transport.fleetops.serviceplan.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class ServicePlanRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newServicePlanRollup("code-844", "name-821")
        val view = toServicePlanRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toServicePlanRollupViews(mutableListOf<ServicePlanRollup>(item, item)).size, "views")
    }
}
