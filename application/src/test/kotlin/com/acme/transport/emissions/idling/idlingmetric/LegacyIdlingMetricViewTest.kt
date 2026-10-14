package com.acme.transport.emissions.idling.idlingmetric

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyIdlingMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyIdlingMetric("code-536", "label-255")
        val view = toLegacyIdlingMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyIdlingMetricViews(mutableListOf<LegacyIdlingMetric>(item, item)).size, "views")
    }
}
