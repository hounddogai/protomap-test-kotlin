package com.acme.transport.emissions.routeoffset.estimate

import kotlin.test.Test
import kotlin.test.assertTrue

class RouteOffsetEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRouteOffsetEstimateEngine()
        val item = newRouteOffsetEstimate("code-851", "name-723")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
