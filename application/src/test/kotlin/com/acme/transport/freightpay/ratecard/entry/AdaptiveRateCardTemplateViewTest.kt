package com.acme.transport.freightpay.ratecard.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveRateCardTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveRateCardTemplate("code-545", "title-644")
        val view = toAdaptiveRateCardTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveRateCardTemplateViews(mutableListOf<AdaptiveRateCardTemplate>(item, item)).size, "views")
    }
}
