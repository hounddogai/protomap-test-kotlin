package com.acme.transport.customs.broker.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class BrokerRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBrokerRequest("code-721", "label-71")
        val view = toBrokerRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBrokerRequestViews(mutableListOf<BrokerRequest>(item, item)).size, "views")
    }
}
