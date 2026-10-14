package com.acme.transport.fleetops.utilization.utilizationestimate

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictUtilizationRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictUtilizationRuleEngine()
        val item = newStrictUtilizationRule("code-540", "title-975")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
