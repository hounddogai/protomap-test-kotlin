package com.acme.transport.emissions.routeoffset.group

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingRouteOffsetPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingRouteOffsetPlanEngine()
        val item = newRollingRouteOffsetPlan("code-420", "label-636")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
