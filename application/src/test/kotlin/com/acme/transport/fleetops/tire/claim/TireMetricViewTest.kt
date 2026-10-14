package com.acme.transport.fleetops.tire.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class TireMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTireMetric("code-878", "title-899")
        val view = toTireMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTireMetricViews(mutableListOf<TireMetric>(item, item)).size, "views")
    }
}
