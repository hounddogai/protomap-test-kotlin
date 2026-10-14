package com.acme.transport.network.zone.mapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ZoneReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultZoneReservationEngine(newRegistry())
        val item = newZoneReservation("code-193", "label-757")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
