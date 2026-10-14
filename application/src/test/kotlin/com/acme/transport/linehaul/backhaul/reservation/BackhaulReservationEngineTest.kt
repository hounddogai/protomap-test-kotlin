package com.acme.transport.linehaul.backhaul.reservation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BackhaulReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBackhaulReservationEngine(newRegistry())
        val item = newBackhaulReservation("code-362", "label-428")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
