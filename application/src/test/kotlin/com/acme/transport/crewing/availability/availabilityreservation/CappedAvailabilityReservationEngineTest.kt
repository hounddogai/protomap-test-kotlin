package com.acme.transport.crewing.availability.availabilityreservation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedAvailabilityReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedAvailabilityReservationEngine(newRegistry())
        val item = newCappedAvailabilityReservation("code-965", "name-64")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
