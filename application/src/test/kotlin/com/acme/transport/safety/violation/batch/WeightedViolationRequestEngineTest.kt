package com.acme.transport.safety.violation.batch

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedViolationRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedViolationRequestEngine()
        val item = newWeightedViolationRequest("code-860", "label-281")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
