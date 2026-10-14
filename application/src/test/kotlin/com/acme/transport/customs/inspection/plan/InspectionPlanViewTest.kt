package com.acme.transport.customs.inspection.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionPlan("code-87", "label-874")
        val view = toInspectionPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionPlanViews(mutableListOf<InspectionPlan>(item, item)).size, "views")
    }
}
