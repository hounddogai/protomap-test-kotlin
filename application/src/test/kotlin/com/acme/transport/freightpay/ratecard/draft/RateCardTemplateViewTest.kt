package com.acme.transport.freightpay.ratecard.draft

import kotlin.test.Test
import kotlin.test.assertEquals

class RateCardTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRateCardTemplate("code-536", "label-30")
        val view = toRateCardTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRateCardTemplateViews(mutableListOf<RateCardTemplate>(item, item)).size, "views")
    }
}
