package com.acme.transport.fleetops.serviceplan.planthreshold

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalServicePlanThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalServicePlanThresholdEngine()
        val item = newSeasonalServicePlanThreshold("code-169", "label-790")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
