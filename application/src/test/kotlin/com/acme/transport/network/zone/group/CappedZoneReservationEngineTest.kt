package com.acme.transport.network.zone.group

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedZoneReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedZoneReservationEngine()
        val item = newCappedZoneReservation("code-730", "label-295")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
