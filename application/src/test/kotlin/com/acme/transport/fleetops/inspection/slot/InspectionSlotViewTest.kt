package com.acme.transport.fleetops.inspection.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionSlot("code-394", "name-645")
        val view = toInspectionSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionSlotViews(mutableListOf<InspectionSlot>(item, item)).size, "views")
    }
}
