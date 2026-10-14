package com.acme.transport.linehaul.capacity.ledger

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictCapacityReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictCapacityReservationEngine()
        val item = newStrictCapacityReservation("code-424", "name-199")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
