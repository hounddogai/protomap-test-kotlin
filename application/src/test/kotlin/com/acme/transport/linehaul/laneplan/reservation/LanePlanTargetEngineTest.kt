package com.acme.transport.linehaul.laneplan.reservation

import kotlin.test.Test
import kotlin.test.assertTrue

class LanePlanTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLanePlanTargetEngine()
        val item = newLanePlanTarget("code-790", "title-45")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
