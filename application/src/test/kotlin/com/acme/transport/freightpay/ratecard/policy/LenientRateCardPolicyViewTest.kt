package com.acme.transport.freightpay.ratecard.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientRateCardPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientRateCardPolicy("code-710", "name-425")
        val view = toLenientRateCardPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientRateCardPolicyViews(mutableListOf<LenientRateCardPolicy>(item, item)).size, "views")
    }
}
