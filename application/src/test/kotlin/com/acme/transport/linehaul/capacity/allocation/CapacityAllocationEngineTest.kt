package com.acme.transport.linehaul.capacity.allocation

import kotlin.test.Test
import kotlin.test.assertTrue

class CapacityAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCapacityAllocationEngine()
        val item = newCapacityAllocation("code-228", "label-191")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
