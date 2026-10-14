package com.acme.transport.customs.inspection.milestone

import kotlin.test.Test
import kotlin.test.assertTrue

class InspectionMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultInspectionMilestoneEngine()
        val item = newInspectionMilestone("code-501", "label-116")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
