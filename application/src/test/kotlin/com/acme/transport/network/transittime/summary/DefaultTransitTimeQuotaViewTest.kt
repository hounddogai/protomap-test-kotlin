package com.acme.transport.network.transittime.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultTransitTimeQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultTransitTimeQuota("code-654", "name-113")
        val view = toDefaultTransitTimeQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultTransitTimeQuotaViews(mutableListOf<DefaultTransitTimeQuota>(item, item)).size, "views")
    }
}
