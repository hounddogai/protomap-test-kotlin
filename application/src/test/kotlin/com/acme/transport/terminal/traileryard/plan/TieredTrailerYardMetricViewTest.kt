package com.acme.transport.terminal.traileryard.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredTrailerYardMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredTrailerYardMetric("code-307", "label-530")
        val view = toTieredTrailerYardMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredTrailerYardMetricViews(mutableListOf<TieredTrailerYardMetric>(item, item)).size, "views")
    }
}
