package com.acme.transport.customs.broker.batch

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualBrokerQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualBrokerQuota("code-864", "name-293")
        val view = toManualBrokerQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualBrokerQuotaViews(mutableListOf<ManualBrokerQuota>(item, item)).size, "views")
    }
}
