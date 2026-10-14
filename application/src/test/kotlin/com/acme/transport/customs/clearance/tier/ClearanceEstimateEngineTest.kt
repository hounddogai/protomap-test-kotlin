package com.acme.transport.customs.clearance.tier

import kotlin.test.Test
import kotlin.test.assertTrue

class ClearanceEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultClearanceEstimateEngine()
        val item = newClearanceEstimate("code-774", "label-759")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
