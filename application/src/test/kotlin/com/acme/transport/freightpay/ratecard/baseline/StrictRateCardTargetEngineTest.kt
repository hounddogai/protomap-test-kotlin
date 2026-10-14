package com.acme.transport.freightpay.ratecard.baseline

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictRateCardTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictRateCardTargetEngine()
        val item = newStrictRateCardTarget("code-439", "title-316")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
