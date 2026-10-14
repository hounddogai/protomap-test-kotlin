package com.acme.transport.safety.violation.violationquota

import kotlin.test.Test
import kotlin.test.assertTrue

class ViolationAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultViolationAssignmentEngine()
        val item = newViolationAssignment("code-109", "label-784")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
