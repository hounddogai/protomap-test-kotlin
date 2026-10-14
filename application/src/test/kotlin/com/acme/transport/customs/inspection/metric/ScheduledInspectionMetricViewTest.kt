package com.acme.transport.customs.inspection.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledInspectionMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledInspectionMetric("code-711", "name-417")
        val view = toScheduledInspectionMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledInspectionMetricViews(mutableListOf<ScheduledInspectionMetric>(item, item)).size, "views")
    }
}
