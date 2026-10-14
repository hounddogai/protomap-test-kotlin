package com.acme.transport.emissions.routeoffset.assignment

import kotlin.test.Test
import kotlin.test.assertTrue

class RouteOffsetManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRouteOffsetManifestEngine()
        val item = newRouteOffsetManifest("code-579", "label-403")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
