package com.acme.transport.network.zone.cycle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredZoneReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredZoneReservationEngine(newRegistry())
        val item = newTieredZoneReservation("code-197", "title-461")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
