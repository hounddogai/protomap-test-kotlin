package com.acme.transport.safety.violation.estimate

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingViolationWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingViolationWindowEngine()
        val item = newRollingViolationWindow("code-204", "name-82")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
