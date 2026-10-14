package com.acme.transport.fleetops.utilization.recorditem

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedUtilizationMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedUtilizationMilestoneEngine()
        val item = newWeightedUtilizationMilestone("code-510", "title-106")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
