package com.acme.transport.freightpay.ratecard.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class RateCardForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRateCardForecast("code-217", "name-506")
        val view = toRateCardForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRateCardForecastViews(mutableListOf<RateCardForecast>(item, item)).size, "views")
    }
}
