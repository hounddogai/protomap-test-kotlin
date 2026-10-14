package com.acme.transport.fleetops.serviceplan.assignment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualServicePlanAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualServicePlanAssignmentEngine(newRegistry())
        val item = newManualServicePlanAssignment("code-611", "title-258")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
