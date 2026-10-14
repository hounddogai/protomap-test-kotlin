package com.acme.transport.fleetops.inspection.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionAdjustment("code-926", "title-509")
        val view = toInspectionAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionAdjustmentViews(mutableListOf<InspectionAdjustment>(item, item)).size, "views")
    }
}
