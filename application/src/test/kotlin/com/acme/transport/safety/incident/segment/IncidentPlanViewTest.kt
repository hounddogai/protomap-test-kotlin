package com.acme.transport.safety.incident.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class IncidentPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newIncidentPlan("code-909", "label-408")
        val view = toIncidentPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toIncidentPlanViews(mutableListOf<IncidentPlan>(item, item)).size, "views")
    }
}
