package com.acme.transport.customs.broker.brokerbatch

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveBrokerMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveBrokerMetric("code-175", "name-43")
        val view = toAdaptiveBrokerMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveBrokerMetricViews(mutableListOf<AdaptiveBrokerMetric>(item, item)).size, "views")
    }
}
