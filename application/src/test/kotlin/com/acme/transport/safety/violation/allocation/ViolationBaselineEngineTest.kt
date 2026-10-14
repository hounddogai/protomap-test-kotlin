package com.acme.transport.safety.violation.allocation

import kotlin.test.Test
import kotlin.test.assertTrue

class ViolationBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultViolationBaselineEngine()
        val item = newViolationBaseline("code-856", "label-739")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
