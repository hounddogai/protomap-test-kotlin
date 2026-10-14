package com.acme.transport.freightpay.ratecard.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class RateCardQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRateCardQuota("code-179", "label-542")
        val view = toRateCardQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRateCardQuotaViews(mutableListOf<RateCardQuota>(item, item)).size, "views")
    }
}
