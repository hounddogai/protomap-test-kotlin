package com.acme.transport.customs.inspection.inspectionmilestone

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveInspectionMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveInspectionMilestoneEngine()
        val item = newAdaptiveInspectionMilestone("code-436", "label-828")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
