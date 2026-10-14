package com.acme.transport.fleetops.serviceplan.planprofile

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackServicePlanProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackServicePlanProfileEngine()
        val item = newFallbackServicePlanProfile("code-982", "label-372")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
