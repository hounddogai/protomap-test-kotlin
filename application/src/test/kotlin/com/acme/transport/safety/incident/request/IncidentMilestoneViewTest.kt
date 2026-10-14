package com.acme.transport.safety.incident.request

import kotlin.test.Test
import kotlin.test.assertEquals

class IncidentMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newIncidentMilestone("code-287", "name-814")
        val view = toIncidentMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toIncidentMilestoneViews(mutableListOf<IncidentMilestone>(item, item)).size, "views")
    }
}
