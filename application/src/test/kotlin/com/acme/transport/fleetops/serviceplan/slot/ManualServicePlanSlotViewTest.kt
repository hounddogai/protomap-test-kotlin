package com.acme.transport.fleetops.serviceplan.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualServicePlanSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualServicePlanSlot("code-326", "label-398")
        val view = toManualServicePlanSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualServicePlanSlotViews(mutableListOf<ManualServicePlanSlot>(item, item)).size, "views")
    }
}
