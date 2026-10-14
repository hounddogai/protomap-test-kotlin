package com.acme.transport.customs.inspection.inspectionmilestone

import kotlin.test.Test
import kotlin.test.assertTrue

class PendingInspectionMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingInspectionMilestoneEngine()
        val item = newPendingInspectionMilestone("code-79", "label-230")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
