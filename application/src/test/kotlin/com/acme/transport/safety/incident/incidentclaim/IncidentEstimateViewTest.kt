package com.acme.transport.safety.incident.incidentclaim

import kotlin.test.Test
import kotlin.test.assertEquals

class IncidentEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newIncidentEstimate("code-700", "title-111")
        val view = toIncidentEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toIncidentEstimateViews(mutableListOf<IncidentEstimate>(item, item)).size, "views")
    }
}
