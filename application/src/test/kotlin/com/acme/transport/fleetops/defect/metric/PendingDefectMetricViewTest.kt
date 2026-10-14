package com.acme.transport.fleetops.defect.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingDefectMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingDefectMetric("code-526", "name-733")
        val view = toPendingDefectMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingDefectMetricViews(mutableListOf<PendingDefectMetric>(item, item)).size, "views")
    }
}
