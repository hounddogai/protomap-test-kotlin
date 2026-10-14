package com.acme.transport.freightpay.ratecard.cardsegment

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedRateCardQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedRateCardQuotaEngine()
        val item = newWeightedRateCardQuota("code-757", "name-771")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
