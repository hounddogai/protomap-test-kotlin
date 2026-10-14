package com.acme.transport.freightpay.ratecard.cardsegment

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultRateCardCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultRateCardCycle("code-172", "name-497")
        val view = toDefaultRateCardCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultRateCardCycleViews(mutableListOf<DefaultRateCardCycle>(item, item)).size, "views")
    }
}
