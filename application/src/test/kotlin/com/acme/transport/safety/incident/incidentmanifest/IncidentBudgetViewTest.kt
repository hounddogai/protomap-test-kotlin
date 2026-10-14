package com.acme.transport.safety.incident.incidentmanifest

import kotlin.test.Test
import kotlin.test.assertEquals

class IncidentBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newIncidentBudget("code-961", "name-849")
        val view = toIncidentBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toIncidentBudgetViews(mutableListOf<IncidentBudget>(item, item)).size, "views")
    }
}
