package com.acme.transport.freightpay.ratecard.cardsegment

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedRateCardDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedRateCardDigestEngine()
        val item = newWeightedRateCardDigest("code-505", "title-970")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
