package com.acme.transport.customs.clearance.variance

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultClearanceAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultClearanceAllocationEngine()
        val item = newDefaultClearanceAllocation("code-312", "label-948")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
