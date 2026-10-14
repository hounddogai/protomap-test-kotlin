package com.acme.transport.safety.violation.plan

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedViolationCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedViolationCycleEngine()
        val item = newWeightedViolationCycle("code-972", "name-307")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
