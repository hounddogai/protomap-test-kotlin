package com.acme.transport.linehaul.backhaul.backhaulreservation

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyBackhaulReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyBackhaulReservationEngine()
        val item = newLegacyBackhaulReservation("code-709", "name-978")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
