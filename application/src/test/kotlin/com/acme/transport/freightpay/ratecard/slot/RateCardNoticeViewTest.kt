package com.acme.transport.freightpay.ratecard.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class RateCardNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRateCardNotice("code-349", "name-709")
        val view = toRateCardNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRateCardNoticeViews(mutableListOf<RateCardNotice>(item, item)).size, "views")
    }
}
