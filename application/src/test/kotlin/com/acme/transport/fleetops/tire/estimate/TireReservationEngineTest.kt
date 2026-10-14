package com.acme.transport.fleetops.tire.estimate

import kotlin.test.Test
import kotlin.test.assertTrue

class TireReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTireReservationEngine()
        val item = newTireReservation("code-547", "title-627")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
