package com.acme.transport.linehaul.trailer.traileroverride

import kotlin.test.Test
import kotlin.test.assertTrue

class TrailerReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTrailerReservationEngine()
        val item = newTrailerReservation("code-780", "name-738")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
