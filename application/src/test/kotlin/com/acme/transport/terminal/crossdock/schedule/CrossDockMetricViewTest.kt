package com.acme.transport.terminal.crossdock.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class CrossDockMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCrossDockMetric("code-284", "label-808")
        val view = toCrossDockMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCrossDockMetricViews(mutableListOf<CrossDockMetric>(item, item)).size, "views")
    }
}
