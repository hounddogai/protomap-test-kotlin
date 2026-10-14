package com.acme.transport.freightpay.ratecard.baseline

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveRateCardBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveRateCardBaseline("code-804", "name-18")
        val view = toAdaptiveRateCardBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveRateCardBaselineViews(mutableListOf<AdaptiveRateCardBaseline>(item, item)).size, "views")
    }
}
