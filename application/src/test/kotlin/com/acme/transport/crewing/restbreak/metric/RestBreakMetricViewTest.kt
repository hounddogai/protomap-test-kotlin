package com.acme.transport.crewing.restbreak.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class RestBreakMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRestBreakMetric("code-136", "name-910")
        val view = toRestBreakMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRestBreakMetricViews(mutableListOf<RestBreakMetric>(item, item)).size, "views")
    }
}
