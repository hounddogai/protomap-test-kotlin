package com.acme.transport.crewing.swap.recorditem

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingSwapPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingSwapPlanEngine()
        val item = newRollingSwapPlan("code-981", "name-594")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
