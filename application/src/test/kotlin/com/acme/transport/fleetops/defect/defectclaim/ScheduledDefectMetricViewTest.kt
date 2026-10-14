package com.acme.transport.fleetops.defect.defectclaim

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledDefectMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledDefectMetric("code-224", "title-547")
        val view = toScheduledDefectMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledDefectMetricViews(mutableListOf<ScheduledDefectMetric>(item, item)).size, "views")
    }
}
