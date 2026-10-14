package com.acme.transport.customs.clearance.clearancemilestone

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveClearanceEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveClearanceEstimateEngine()
        val item = newAdaptiveClearanceEstimate("code-926", "label-703")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
