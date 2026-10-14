package com.acme.transport.customs.broker.batch

import kotlin.test.Test
import kotlin.test.assertEquals

class BrokerBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBrokerBatch("code-6", "label-893")
        val view = toBrokerBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBrokerBatchViews(mutableListOf<BrokerBatch>(item, item)).size, "views")
    }
}
