package com.acme.transport.fleetops.tire.reservation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyTireReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyTireReservationEngine(newRegistry())
        val item = newLegacyTireReservation("code-401", "name-273")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
