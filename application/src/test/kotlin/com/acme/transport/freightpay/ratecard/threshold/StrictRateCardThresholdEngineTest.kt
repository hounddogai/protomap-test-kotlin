package com.acme.transport.freightpay.ratecard.threshold

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictRateCardThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictRateCardThresholdEngine()
        val item = newStrictRateCardThreshold("code-991", "label-413")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
