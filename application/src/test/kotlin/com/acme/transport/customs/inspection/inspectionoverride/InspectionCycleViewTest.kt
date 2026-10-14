package com.acme.transport.customs.inspection.inspectionoverride

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionCycle("code-566", "label-444")
        val view = toInspectionCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionCycleViews(mutableListOf<InspectionCycle>(item, item)).size, "views")
    }
}
