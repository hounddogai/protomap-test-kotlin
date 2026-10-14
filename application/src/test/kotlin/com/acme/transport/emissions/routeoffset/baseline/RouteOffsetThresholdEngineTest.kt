package com.acme.transport.emissions.routeoffset.baseline

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RouteOffsetThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRouteOffsetThresholdEngine(newRegistry())
        val item = newRouteOffsetThreshold("code-274", "label-479")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
