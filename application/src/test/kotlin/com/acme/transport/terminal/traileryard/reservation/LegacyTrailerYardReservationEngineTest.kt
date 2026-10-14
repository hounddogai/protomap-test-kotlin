package com.acme.transport.terminal.traileryard.reservation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyTrailerYardReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyTrailerYardReservationEngine(newRegistry())
        val item = newLegacyTrailerYardReservation("code-433", "name-658")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
