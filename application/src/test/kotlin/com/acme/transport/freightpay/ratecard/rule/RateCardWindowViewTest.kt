package com.acme.transport.freightpay.ratecard.rule

import kotlin.test.Test
import kotlin.test.assertEquals

class RateCardWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRateCardWindow("code-248", "name-417")
        val view = toRateCardWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRateCardWindowViews(mutableListOf<RateCardWindow>(item, item)).size, "views")
    }
}
