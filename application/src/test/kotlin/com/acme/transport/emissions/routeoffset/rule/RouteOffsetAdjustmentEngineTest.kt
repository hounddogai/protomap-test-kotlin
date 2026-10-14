package com.acme.transport.emissions.routeoffset.rule

import kotlin.test.Test
import kotlin.test.assertTrue

class RouteOffsetAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRouteOffsetAdjustmentEngine()
        val item = newRouteOffsetAdjustment("code-735", "title-518")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
