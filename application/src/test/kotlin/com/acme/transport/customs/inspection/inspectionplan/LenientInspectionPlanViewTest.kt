package com.acme.transport.customs.inspection.inspectionplan

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientInspectionPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientInspectionPlan("code-866", "title-387")
        val view = toLenientInspectionPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientInspectionPlanViews(mutableListOf<LenientInspectionPlan>(item, item)).size, "views")
    }
}
