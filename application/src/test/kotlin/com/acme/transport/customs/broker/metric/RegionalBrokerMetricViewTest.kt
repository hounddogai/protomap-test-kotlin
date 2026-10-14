package com.acme.transport.customs.broker.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalBrokerMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalBrokerMetric("code-516", "name-933")
        val view = toRegionalBrokerMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalBrokerMetricViews(mutableListOf<RegionalBrokerMetric>(item, item)).size, "views")
    }
}
