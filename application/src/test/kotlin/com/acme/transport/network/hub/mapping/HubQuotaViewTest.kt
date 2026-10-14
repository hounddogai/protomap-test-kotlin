package com.acme.transport.network.hub.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class HubQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newHubQuota("code-511", "name-922")
        val view = toHubQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toHubQuotaViews(mutableListOf<HubQuota>(item, item)).size, "views")
    }
}
