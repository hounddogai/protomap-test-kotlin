package com.acme.transport.freightpay.ratecard.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class RateCardTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRateCardTarget("code-139", "name-527")
        val view = toRateCardTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRateCardTargetViews(mutableListOf<RateCardTarget>(item, item)).size, "views")
    }
}
