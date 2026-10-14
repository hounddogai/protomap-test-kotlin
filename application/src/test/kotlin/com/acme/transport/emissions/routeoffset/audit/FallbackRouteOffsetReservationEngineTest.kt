package com.acme.transport.emissions.routeoffset.audit

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackRouteOffsetReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackRouteOffsetReservationEngine()
        val item = newFallbackRouteOffsetReservation("code-83", "title-666")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
