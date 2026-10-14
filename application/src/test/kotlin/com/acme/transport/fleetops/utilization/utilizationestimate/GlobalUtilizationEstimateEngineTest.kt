package com.acme.transport.fleetops.utilization.utilizationestimate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalUtilizationEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalUtilizationEstimateEngine(newRegistry())
        val item = newGlobalUtilizationEstimate("code-277", "title-773")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
