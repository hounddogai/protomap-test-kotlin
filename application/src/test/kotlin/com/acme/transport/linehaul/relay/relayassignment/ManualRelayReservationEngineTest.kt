package com.acme.transport.linehaul.relay.relayassignment

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualRelayReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualRelayReservationEngine()
        val item = newManualRelayReservation("code-960", "title-162")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
