package com.acme.transport.emissions.routeoffset.rollup

import kotlin.test.Test
import kotlin.test.assertTrue

class RouteOffsetProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRouteOffsetProfileEngine()
        val item = newRouteOffsetProfile("code-145", "label-267")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
