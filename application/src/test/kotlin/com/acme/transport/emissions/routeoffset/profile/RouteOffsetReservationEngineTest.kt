package com.acme.transport.emissions.routeoffset.profile

import kotlin.test.Test
import kotlin.test.assertTrue

class RouteOffsetReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRouteOffsetReservationEngine()
        val item = newRouteOffsetReservation("code-376", "name-386")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
