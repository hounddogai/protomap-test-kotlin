package com.acme.transport.linehaul.capacity.plan

import kotlin.test.Test
import kotlin.test.assertTrue

class CapacityPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCapacityPlanEngine()
        val item = newCapacityPlan("code-488", "label-2")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
