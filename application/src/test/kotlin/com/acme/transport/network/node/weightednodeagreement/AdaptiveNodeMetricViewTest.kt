package com.acme.transport.network.node.weightednodeagreement

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveNodeMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveNodeMetric("code-220", "title-384")
        val view = toAdaptiveNodeMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveNodeMetricViews(mutableListOf<AdaptiveNodeMetric>(item, item)).size, "views")
    }
}
