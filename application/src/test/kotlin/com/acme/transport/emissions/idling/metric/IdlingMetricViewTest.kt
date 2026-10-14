package com.acme.transport.emissions.idling.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class IdlingMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newIdlingMetric("code-783", "title-935")
        val view = toIdlingMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toIdlingMetricViews(mutableListOf<IdlingMetric>(item, item)).size, "views")
    }
}
