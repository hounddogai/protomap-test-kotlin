package com.acme.transport.emissions.routeoffset.baseline

import kotlin.test.Test
import kotlin.test.assertTrue

class RouteOffsetSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRouteOffsetSnapshotEngine()
        val item = newRouteOffsetSnapshot("code-520", "name-564")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
