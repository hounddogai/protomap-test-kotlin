package com.acme.transport.customs.broker.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class BrokerMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBrokerMetric("code-173", "label-592")
        val view = toBrokerMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBrokerMetricViews(mutableListOf<BrokerMetric>(item, item)).size, "views")
    }
}
