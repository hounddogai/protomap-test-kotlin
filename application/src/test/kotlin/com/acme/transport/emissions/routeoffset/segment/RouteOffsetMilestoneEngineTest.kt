package com.acme.transport.emissions.routeoffset.segment

import kotlin.test.Test
import kotlin.test.assertTrue

class RouteOffsetMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRouteOffsetMilestoneEngine()
        val item = newRouteOffsetMilestone("code-958", "title-562")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
