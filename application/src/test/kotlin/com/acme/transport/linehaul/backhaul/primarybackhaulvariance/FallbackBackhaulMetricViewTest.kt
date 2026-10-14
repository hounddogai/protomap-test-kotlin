package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackBackhaulMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackBackhaulMetric("code-116", "label-741")
        val view = toFallbackBackhaulMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackBackhaulMetricViews(mutableListOf<FallbackBackhaulMetric>(item, item)).size, "views")
    }
}
