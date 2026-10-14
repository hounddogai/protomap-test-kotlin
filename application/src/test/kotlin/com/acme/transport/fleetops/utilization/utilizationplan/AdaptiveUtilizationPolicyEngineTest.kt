package com.acme.transport.fleetops.utilization.utilizationplan

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveUtilizationPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveUtilizationPolicyEngine()
        val item = newAdaptiveUtilizationPolicy("code-45", "label-328")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
