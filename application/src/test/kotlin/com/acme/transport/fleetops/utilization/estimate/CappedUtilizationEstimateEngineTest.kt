package com.acme.transport.fleetops.utilization.estimate

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedUtilizationEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedUtilizationEstimateEngine()
        val item = newCappedUtilizationEstimate("code-992", "label-945")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
