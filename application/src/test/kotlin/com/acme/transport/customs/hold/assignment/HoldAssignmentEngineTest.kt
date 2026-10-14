package com.acme.transport.customs.hold.assignment

import kotlin.test.Test
import kotlin.test.assertTrue

class HoldAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultHoldAssignmentEngine()
        val item = newHoldAssignment("code-593", "title-737")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
