package com.acme.transport.fleetops.defect.notice

import kotlin.test.Test
import kotlin.test.assertEquals

class DefectMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefectMetric("code-408", "name-609")
        val view = toDefectMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefectMetricViews(mutableListOf<DefectMetric>(item, item)).size, "views")
    }
}
