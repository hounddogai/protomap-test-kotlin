package com.acme.transport.fleetops.tire.baseline

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingTireMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingTireMetric("code-702", "name-477")
        val view = toPendingTireMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingTireMetricViews(mutableListOf<PendingTireMetric>(item, item)).size, "views")
    }
}
