package com.acme.transport.lastmile.redelivery.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class RedeliveryMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRedeliveryMetric("code-977", "title-313")
        val view = toRedeliveryMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRedeliveryMetricViews(mutableListOf<RedeliveryMetric>(item, item)).size, "views")
    }
}
