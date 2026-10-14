package com.acme.transport.freightpay.ratecard.request

import kotlin.test.Test
import kotlin.test.assertEquals

class RateCardAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRateCardAgreement("code-17", "name-238")
        val view = toRateCardAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRateCardAgreementViews(mutableListOf<RateCardAgreement>(item, item)).size, "views")
    }
}
