package com.acme.transport.fleetops.tire.tiremanifest

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedTireMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedTireMilestoneEngine()
        val item = newWeightedTireMilestone("code-251", "name-658")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
