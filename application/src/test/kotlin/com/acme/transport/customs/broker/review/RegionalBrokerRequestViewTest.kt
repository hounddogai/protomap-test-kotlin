package com.acme.transport.customs.broker.review

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalBrokerRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalBrokerRequest("code-291", "title-771")
        val view = toRegionalBrokerRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalBrokerRequestViews(mutableListOf<RegionalBrokerRequest>(item, item)).size, "views")
    }
}
