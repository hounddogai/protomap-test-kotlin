package com.acme.transport.linehaul.laneplan.planbudget

import kotlin.test.Test
import kotlin.test.assertTrue

class LanePlanVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLanePlanVarianceEngine()
        val item = newLanePlanVariance("code-69", "title-869")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
