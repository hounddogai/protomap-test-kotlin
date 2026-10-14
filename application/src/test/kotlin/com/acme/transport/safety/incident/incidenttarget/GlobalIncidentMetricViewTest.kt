package com.acme.transport.safety.incident.incidenttarget

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalIncidentMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalIncidentMetric("code-338", "title-70")
        val view = toGlobalIncidentMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalIncidentMetricViews(mutableListOf<GlobalIncidentMetric>(item, item)).size, "views")
    }
}
