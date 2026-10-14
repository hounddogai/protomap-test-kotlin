package com.acme.transport.customs.inspection.plan

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionPlanEngine(newRegistry())
        val item = newInspectionPlan("code-87", "label-874")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
