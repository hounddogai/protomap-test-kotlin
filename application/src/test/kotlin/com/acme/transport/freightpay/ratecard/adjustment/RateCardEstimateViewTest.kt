package com.acme.transport.freightpay.ratecard.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class RateCardEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRateCardEstimate("code-584", "name-532")
        val view = toRateCardEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRateCardEstimateViews(mutableListOf<RateCardEstimate>(item, item)).size, "views")
    }
}
