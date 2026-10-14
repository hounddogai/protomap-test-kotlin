package com.acme.transport.fleetops.utilization.utilizationsummary

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveUtilizationSummaryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveUtilizationSummaryEngine()
        val item = newAdaptiveUtilizationSummary("code-34", "name-565")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
