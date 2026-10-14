package com.acme.transport.fleetops.utilization.milestone

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingUtilizationQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingUtilizationQuotaEngine()
        val item = newRollingUtilizationQuota("code-413", "name-979")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
