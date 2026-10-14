package com.acme.transport.fleetops.inspection.profile

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedInspectionBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedInspectionBaselineEngine()
        val item = newWeightedInspectionBaseline("code-144", "label-855")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
