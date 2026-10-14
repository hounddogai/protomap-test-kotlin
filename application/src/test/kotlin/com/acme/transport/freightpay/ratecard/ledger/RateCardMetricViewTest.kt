package com.acme.transport.freightpay.ratecard.ledger

import kotlin.test.Test
import kotlin.test.assertEquals

class RateCardMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRateCardMetric("code-864", "title-352")
        val view = toRateCardMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRateCardMetricViews(mutableListOf<RateCardMetric>(item, item)).size, "views")
    }
}
