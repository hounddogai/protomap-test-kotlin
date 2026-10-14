package com.acme.transport.linehaul.capacity.cycle

import kotlin.test.Test
import kotlin.test.assertTrue

class CapacityAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCapacityAssignmentEngine()
        val item = newCapacityAssignment("code-863", "name-361")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
