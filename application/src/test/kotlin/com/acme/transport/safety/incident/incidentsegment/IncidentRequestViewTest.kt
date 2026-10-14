package com.acme.transport.safety.incident.incidentsegment

import kotlin.test.Test
import kotlin.test.assertEquals

class IncidentRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newIncidentRequest("code-654", "title-81")
        val view = toIncidentRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toIncidentRequestViews(mutableListOf<IncidentRequest>(item, item)).size, "views")
    }
}
