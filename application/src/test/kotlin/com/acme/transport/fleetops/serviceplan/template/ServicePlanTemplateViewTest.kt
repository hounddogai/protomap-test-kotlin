package com.acme.transport.fleetops.serviceplan.template

import kotlin.test.Test
import kotlin.test.assertEquals

class ServicePlanTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newServicePlanTemplate("code-195", "name-556")
        val view = toServicePlanTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toServicePlanTemplateViews(mutableListOf<ServicePlanTemplate>(item, item)).size, "views")
    }
}
