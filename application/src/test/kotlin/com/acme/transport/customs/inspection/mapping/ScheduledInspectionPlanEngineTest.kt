package com.acme.transport.customs.inspection.mapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledInspectionPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledInspectionPlanEngine(newRegistry())
        val item = newScheduledInspectionPlan("code-726", "name-412")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
