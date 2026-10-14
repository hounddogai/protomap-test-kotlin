package com.acme.transport.customs.broker.quota

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultBrokerQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultBrokerQuota("code-494", "name-313")
        val view = toDefaultBrokerQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultBrokerQuotaViews(mutableListOf<DefaultBrokerQuota>(item, item)).size, "views")
    }
}
