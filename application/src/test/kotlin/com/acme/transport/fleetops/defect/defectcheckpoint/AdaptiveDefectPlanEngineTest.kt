package com.acme.transport.fleetops.defect.defectcheckpoint

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveDefectPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveDefectPlanEngine(newRegistry())
        val item = newAdaptiveDefectPlan("code-242", "name-357")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
