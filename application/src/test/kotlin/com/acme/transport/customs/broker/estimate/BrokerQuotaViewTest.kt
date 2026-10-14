package com.acme.transport.customs.broker.estimate

import kotlin.test.Test
import kotlin.test.assertEquals

class BrokerQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBrokerQuota("code-943", "name-29")
        val view = toBrokerQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBrokerQuotaViews(mutableListOf<BrokerQuota>(item, item)).size, "views")
    }
}
