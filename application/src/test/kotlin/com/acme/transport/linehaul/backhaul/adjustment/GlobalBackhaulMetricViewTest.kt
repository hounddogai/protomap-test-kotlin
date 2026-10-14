package com.acme.transport.linehaul.backhaul.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalBackhaulMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalBackhaulMetric("code-441", "label-589")
        val view = toGlobalBackhaulMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalBackhaulMetricViews(mutableListOf<GlobalBackhaulMetric>(item, item)).size, "views")
    }
}
