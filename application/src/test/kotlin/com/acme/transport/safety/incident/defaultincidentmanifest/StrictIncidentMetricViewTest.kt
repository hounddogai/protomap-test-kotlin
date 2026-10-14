package com.acme.transport.safety.incident.defaultincidentmanifest

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictIncidentMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictIncidentMetric("code-776", "label-102")
        val view = toStrictIncidentMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictIncidentMetricViews(mutableListOf<StrictIncidentMetric>(item, item)).size, "views")
    }
}
