package com.acme.transport.terminal.dockdoor.review

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalDockDoorMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalDockDoorMetric("code-414", "title-368")
        val view = toRegionalDockDoorMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalDockDoorMetricViews(mutableListOf<RegionalDockDoorMetric>(item, item)).size, "views")
    }
}
