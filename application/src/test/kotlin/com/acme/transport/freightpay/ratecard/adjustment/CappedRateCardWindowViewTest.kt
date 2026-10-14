package com.acme.transport.freightpay.ratecard.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedRateCardWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedRateCardWindow("code-86", "title-71")
        val view = toCappedRateCardWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedRateCardWindowViews(mutableListOf<CappedRateCardWindow>(item, item)).size, "views")
    }
}
