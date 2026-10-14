package com.acme.transport.linehaul.laneplan.planreservation

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveLanePlanBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveLanePlanBundleEngine()
        val item = newAdaptiveLanePlanBundle("code-847", "label-973")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
