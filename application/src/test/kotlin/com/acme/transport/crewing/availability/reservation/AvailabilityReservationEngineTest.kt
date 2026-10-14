package com.acme.transport.crewing.availability.reservation

import kotlin.test.Test
import kotlin.test.assertTrue

class AvailabilityReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAvailabilityReservationEngine()
        val item = newAvailabilityReservation("code-512", "label-175")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
