package com.acme.transport.safety.violation.violationcycle

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictViolationCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictViolationCycleEngine()
        val item = newStrictViolationCycle("code-298", "name-730")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
