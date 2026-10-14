package com.acme.transport.emissions.routeoffset.metric

import kotlin.test.Test
import kotlin.test.assertTrue

class RouteOffsetClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRouteOffsetClaimEngine()
        val item = newRouteOffsetClaim("code-260", "label-363")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
