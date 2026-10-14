package com.acme.transport.linehaul.laneplan.overrideitem

import kotlin.test.Test
import kotlin.test.assertTrue

class LanePlanMetricEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLanePlanMetricEngine()
        val item = newLanePlanMetric("code-58", "name-679")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
