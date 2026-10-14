package com.acme.transport.network.transittime.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class TransitTimeMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTransitTimeMetric("code-830", "title-585")
        val view = toTransitTimeMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTransitTimeMetricViews(mutableListOf<TransitTimeMetric>(item, item)).size, "views")
    }
}
