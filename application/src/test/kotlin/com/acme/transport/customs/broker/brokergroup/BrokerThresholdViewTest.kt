package com.acme.transport.customs.broker.brokergroup

import kotlin.test.Test
import kotlin.test.assertEquals

class BrokerThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBrokerThreshold("code-84", "title-238")
        val view = toBrokerThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBrokerThresholdViews(mutableListOf<BrokerThreshold>(item, item)).size, "views")
    }
}
