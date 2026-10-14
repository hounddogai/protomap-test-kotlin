package com.acme.transport.emissions.evcharge.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingEvChargeMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingEvChargeMetric("code-57", "label-639")
        val view = toRollingEvChargeMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingEvChargeMetricViews(mutableListOf<RollingEvChargeMetric>(item, item)).size, "views")
    }
}
