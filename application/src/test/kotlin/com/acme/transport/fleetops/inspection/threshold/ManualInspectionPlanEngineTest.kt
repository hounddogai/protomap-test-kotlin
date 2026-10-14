package com.acme.transport.fleetops.inspection.threshold

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualInspectionPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualInspectionPlanEngine(newRegistry())
        val item = newManualInspectionPlan("code-502", "label-530")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
