package com.acme.transport.customs.broker.request

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledBrokerRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledBrokerRequest("code-925", "name-958")
        val view = toScheduledBrokerRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledBrokerRequestViews(mutableListOf<ScheduledBrokerRequest>(item, item)).size, "views")
    }
}
