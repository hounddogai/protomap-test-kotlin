package com.acme.transport.emissions.evcharge.chargeslot

import kotlin.test.Test
import kotlin.test.assertEquals

class EvChargeQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newEvChargeQuota("code-737", "title-423")
        val view = toEvChargeQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toEvChargeQuotaViews(mutableListOf<EvChargeQuota>(item, item)).size, "views")
    }
}
