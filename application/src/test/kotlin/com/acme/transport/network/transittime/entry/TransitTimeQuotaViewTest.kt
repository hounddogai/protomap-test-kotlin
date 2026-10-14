package com.acme.transport.network.transittime.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class TransitTimeQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTransitTimeQuota("code-208", "title-917")
        val view = toTransitTimeQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTransitTimeQuotaViews(mutableListOf<TransitTimeQuota>(item, item)).size, "views")
    }
}
