package com.acme.transport.customs.inspection.inspectionplan

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientInspectionPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientInspectionPlanEngine(newRegistry())
        val item = newLenientInspectionPlan("code-866", "title-387")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
