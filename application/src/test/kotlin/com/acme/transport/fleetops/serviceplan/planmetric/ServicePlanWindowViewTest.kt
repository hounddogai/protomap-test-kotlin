package com.acme.transport.fleetops.serviceplan.planmetric

import kotlin.test.Test
import kotlin.test.assertEquals

class ServicePlanWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newServicePlanWindow("code-965", "name-755")
        val view = toServicePlanWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toServicePlanWindowViews(mutableListOf<ServicePlanWindow>(item, item)).size, "views")
    }
}
