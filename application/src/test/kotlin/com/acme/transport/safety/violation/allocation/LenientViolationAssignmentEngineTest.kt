package com.acme.transport.safety.violation.allocation

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientViolationAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientViolationAssignmentEngine()
        val item = newLenientViolationAssignment("code-459", "label-834")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
