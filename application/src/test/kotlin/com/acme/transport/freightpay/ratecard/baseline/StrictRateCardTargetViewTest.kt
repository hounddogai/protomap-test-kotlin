package com.acme.transport.freightpay.ratecard.baseline

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictRateCardTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictRateCardTarget("code-439", "title-316")
        val view = toStrictRateCardTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictRateCardTargetViews(mutableListOf<StrictRateCardTarget>(item, item)).size, "views")
    }
}
