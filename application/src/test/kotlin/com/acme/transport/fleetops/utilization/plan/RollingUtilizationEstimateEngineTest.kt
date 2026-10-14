package com.acme.transport.fleetops.utilization.plan

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingUtilizationEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingUtilizationEstimateEngine(newRegistry())
        val item = newRollingUtilizationEstimate("code-559", "name-188")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
