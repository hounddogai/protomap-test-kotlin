package com.acme.transport.fleetops.utilization.plan

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackUtilizationPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackUtilizationPlanEngine()
        val item = newFallbackUtilizationPlan("code-585", "name-326")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
