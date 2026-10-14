package com.acme.transport.freightpay.ratecard.target

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyRateCardTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyRateCardTarget("code-322", "label-366")
        val view = toLegacyRateCardTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyRateCardTargetViews(mutableListOf<LegacyRateCardTarget>(item, item)).size, "views")
    }
}
