package com.acme.transport.linehaul.laneplan.planthreshold

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredLanePlanThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredLanePlanThresholdEngine()
        val item = newTieredLanePlanThreshold("code-56", "name-465")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
