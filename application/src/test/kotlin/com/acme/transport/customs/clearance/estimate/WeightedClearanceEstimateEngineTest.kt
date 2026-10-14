package com.acme.transport.customs.clearance.estimate

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedClearanceEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedClearanceEstimateEngine()
        val item = newWeightedClearanceEstimate("code-354", "name-290")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
