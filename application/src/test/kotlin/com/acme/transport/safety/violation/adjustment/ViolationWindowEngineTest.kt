package com.acme.transport.safety.violation.adjustment

import kotlin.test.Test
import kotlin.test.assertTrue

class ViolationWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultViolationWindowEngine()
        val item = newViolationWindow("code-90", "name-792")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
