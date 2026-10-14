package com.acme.transport.freightpay.ratecard.review

import kotlin.test.Test
import kotlin.test.assertEquals

class RateCardPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRateCardPolicy("code-654", "title-367")
        val view = toRateCardPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRateCardPolicyViews(mutableListOf<RateCardPolicy>(item, item)).size, "views")
    }
}
