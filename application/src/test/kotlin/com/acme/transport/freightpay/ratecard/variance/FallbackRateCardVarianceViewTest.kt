package com.acme.transport.freightpay.ratecard.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackRateCardVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackRateCardVariance("code-643", "title-395")
        val view = toFallbackRateCardVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackRateCardVarianceViews(mutableListOf<FallbackRateCardVariance>(item, item)).size, "views")
    }
}
