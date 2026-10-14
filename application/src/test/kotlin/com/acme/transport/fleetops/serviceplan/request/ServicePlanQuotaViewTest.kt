package com.acme.transport.fleetops.serviceplan.request

import kotlin.test.Test
import kotlin.test.assertEquals

class ServicePlanQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newServicePlanQuota("code-548", "label-33")
        val view = toServicePlanQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toServicePlanQuotaViews(mutableListOf<ServicePlanQuota>(item, item)).size, "views")
    }
}
