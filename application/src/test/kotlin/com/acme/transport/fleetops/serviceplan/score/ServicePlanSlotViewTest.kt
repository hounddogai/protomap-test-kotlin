package com.acme.transport.fleetops.serviceplan.score

import kotlin.test.Test
import kotlin.test.assertEquals

class ServicePlanSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newServicePlanSlot("code-113", "label-770")
        val view = toServicePlanSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toServicePlanSlotViews(mutableListOf<ServicePlanSlot>(item, item)).size, "views")
    }
}
