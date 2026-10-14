package com.acme.transport.fleetops.defect.defectforecast

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultDefectPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultDefectPlan("code-571", "title-602")
        val view = toDefaultDefectPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultDefectPlanViews(mutableListOf<DefaultDefectPlan>(item, item)).size, "views")
    }
}
