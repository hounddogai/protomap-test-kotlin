package com.acme.transport.safety.incident.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyIncidentMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyIncidentMetric("code-401", "title-992")
        val view = toLegacyIncidentMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyIncidentMetricViews(mutableListOf<LegacyIncidentMetric>(item, item)).size, "views")
    }
}
