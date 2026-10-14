package com.acme.transport.safety.violation.plan

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyViolationCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyViolationCycleEngine()
        val item = newLegacyViolationCycle("code-17", "label-687")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
