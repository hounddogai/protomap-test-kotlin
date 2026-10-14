package com.acme.transport.fleetops.serviceplan.planrollup

import kotlin.test.Test
import kotlin.test.assertEquals

class ServicePlanRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newServicePlanRecord("code-84", "name-314")
        val view = toServicePlanRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toServicePlanRecordViews(mutableListOf<ServicePlanRecord>(item, item)).size, "views")
    }
}
