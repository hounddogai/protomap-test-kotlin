package com.acme.transport.crewing.roster.tier

import kotlin.test.Test
import kotlin.test.assertTrue

class RosterReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRosterReservationEngine()
        val item = newRosterReservation("code-494", "title-878")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
