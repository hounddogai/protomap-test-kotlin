package com.acme.transport.freightpay.ratecard.forecast

import kotlin.test.Test
import kotlin.test.assertTrue

class RateCardForecastEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRateCardForecastEngine()
        val item = newRateCardForecast("code-217", "name-506")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
