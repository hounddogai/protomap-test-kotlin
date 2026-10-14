package com.acme.transport.network.node.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackNodeMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackNodeMetric("code-722", "title-820")
        val view = toFallbackNodeMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackNodeMetricViews(mutableListOf<FallbackNodeMetric>(item, item)).size, "views")
    }
}
