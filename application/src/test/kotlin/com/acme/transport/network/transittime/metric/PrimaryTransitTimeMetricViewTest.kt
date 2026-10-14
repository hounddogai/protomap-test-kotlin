package com.acme.transport.network.transittime.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryTransitTimeMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryTransitTimeMetric("code-421", "name-337")
        val view = toPrimaryTransitTimeMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryTransitTimeMetricViews(mutableListOf<PrimaryTransitTimeMetric>(item, item)).size, "views")
    }
}
