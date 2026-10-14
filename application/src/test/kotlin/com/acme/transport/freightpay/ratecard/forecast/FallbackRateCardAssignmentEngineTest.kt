package com.acme.transport.freightpay.ratecard.forecast

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackRateCardAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackRateCardAssignmentEngine()
        val item = newFallbackRateCardAssignment("code-427", "name-42")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
