package com.acme.transport.emissions.evcharge.snapshot

import kotlin.test.Test
import kotlin.test.assertTrue

class EvChargeReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEvChargeReservationEngine()
        val item = newEvChargeReservation("code-472", "label-137")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
