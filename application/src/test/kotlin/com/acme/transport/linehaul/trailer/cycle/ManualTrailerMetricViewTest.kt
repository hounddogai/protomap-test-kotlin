package com.acme.transport.linehaul.trailer.cycle

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualTrailerMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualTrailerMetric("code-627", "name-657")
        val view = toManualTrailerMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualTrailerMetricViews(mutableListOf<ManualTrailerMetric>(item, item)).size, "views")
    }
}
