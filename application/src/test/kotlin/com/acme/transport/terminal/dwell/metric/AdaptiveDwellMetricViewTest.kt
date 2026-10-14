package com.acme.transport.terminal.dwell.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveDwellMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveDwellMetric("code-503", "name-336")
        val view = toAdaptiveDwellMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveDwellMetricViews(mutableListOf<AdaptiveDwellMetric>(item, item)).size, "views")
    }
}
