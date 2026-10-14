package com.acme.transport.fleetops.utilization.utilizationplan

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualUtilizationPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualUtilizationPlanEngine(newRegistry())
        val item = newManualUtilizationPlan("code-757", "name-490")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
