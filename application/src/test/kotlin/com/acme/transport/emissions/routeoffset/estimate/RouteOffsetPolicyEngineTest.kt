package com.acme.transport.emissions.routeoffset.estimate

import kotlin.test.Test
import kotlin.test.assertTrue

class RouteOffsetPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRouteOffsetPolicyEngine()
        val item = newRouteOffsetPolicy("code-486", "name-91")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
