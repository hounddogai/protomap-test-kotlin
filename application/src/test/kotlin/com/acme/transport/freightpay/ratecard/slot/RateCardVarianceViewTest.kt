package com.acme.transport.freightpay.ratecard.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class RateCardVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRateCardVariance("code-807", "name-343")
        val view = toRateCardVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRateCardVarianceViews(mutableListOf<RateCardVariance>(item, item)).size, "views")
    }
}
