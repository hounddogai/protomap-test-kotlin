package com.acme.transport.freightpay.ratecard.segment

import kotlin.test.Test
import kotlin.test.assertTrue

class RateCardBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRateCardBaselineEngine()
        val item = newRateCardBaseline("code-390", "label-569")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
