package com.acme.transport.safety.violation.plan

import kotlin.test.Test
import kotlin.test.assertTrue

class ViolationPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultViolationPolicyEngine()
        val item = newViolationPolicy("code-504", "title-152")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
