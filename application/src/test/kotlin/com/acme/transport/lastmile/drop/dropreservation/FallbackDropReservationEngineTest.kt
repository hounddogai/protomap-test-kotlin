package com.acme.transport.lastmile.drop.dropreservation

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackDropReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackDropReservationEngine()
        val item = newFallbackDropReservation("code-949", "title-702")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
