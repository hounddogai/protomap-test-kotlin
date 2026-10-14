package com.acme.transport.emissions.idling.idlingwindow

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultIdlingMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultIdlingMetric("code-143", "title-308")
        val view = toDefaultIdlingMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultIdlingMetricViews(mutableListOf<DefaultIdlingMetric>(item, item)).size, "views")
    }
}
