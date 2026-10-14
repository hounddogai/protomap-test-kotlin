package com.acme.transport.fleetops.inspection.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionPlan("code-494", "name-335")
        val view = toInspectionPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionPlanViews(mutableListOf<InspectionPlan>(item, item)).size, "views")
    }
}
