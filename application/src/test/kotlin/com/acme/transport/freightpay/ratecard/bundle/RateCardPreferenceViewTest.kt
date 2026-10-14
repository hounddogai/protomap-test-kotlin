package com.acme.transport.freightpay.ratecard.bundle

import kotlin.test.Test
import kotlin.test.assertEquals

class RateCardPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRateCardPreference("code-474", "label-345")
        val view = toRateCardPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRateCardPreferenceViews(mutableListOf<RateCardPreference>(item, item)).size, "views")
    }
}
