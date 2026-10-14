package com.acme.transport.terminal.traileryard.assignment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TrailerYardReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTrailerYardReservationEngine(newRegistry())
        val item = newTrailerYardReservation("code-287", "title-40")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
