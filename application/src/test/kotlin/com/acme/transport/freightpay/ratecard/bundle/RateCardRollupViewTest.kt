package com.acme.transport.freightpay.ratecard.bundle

import kotlin.test.Test
import kotlin.test.assertEquals

class RateCardRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRateCardRollup("code-942", "label-936")
        val view = toRateCardRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRateCardRollupViews(mutableListOf<RateCardRollup>(item, item)).size, "views")
    }
}
