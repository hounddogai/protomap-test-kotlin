package com.acme.transport.fleetops.serviceplan.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals

class ServicePlanExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newServicePlanException("code-134", "label-283")
        val view = toServicePlanExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toServicePlanExceptionViews(mutableListOf<ServicePlanException>(item, item)).size, "views")
    }
}
