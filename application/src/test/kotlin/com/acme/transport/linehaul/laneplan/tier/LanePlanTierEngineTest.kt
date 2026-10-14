package com.acme.transport.linehaul.laneplan.tier

import kotlin.test.Test
import kotlin.test.assertTrue

class LanePlanTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLanePlanTierEngine()
        val item = newLanePlanTier("code-695", "name-714")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
