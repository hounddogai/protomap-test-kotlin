package com.acme.transport.linehaul.laneplan.cycle

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedLanePlanThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedLanePlanThresholdEngine()
        val item = newCappedLanePlanThreshold("code-507", "name-18")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
