package com.acme.transport.linehaul.backhaul.allocation

import kotlin.test.Test
import kotlin.test.assertTrue

class BackhaulAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBackhaulAllocationEngine()
        val item = newBackhaulAllocation("code-624", "label-124")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
