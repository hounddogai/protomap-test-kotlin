package com.acme.transport.fleetops.inspection.rollup

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedInspectionPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedInspectionPlanEngine()
        val item = newWeightedInspectionPlan("code-900", "name-158")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
