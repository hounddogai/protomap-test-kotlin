package com.acme.transport.crewing.roster.rosterforecast

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictRosterReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictRosterReservationEngine(newRegistry())
        val item = newStrictRosterReservation("code-625", "name-303")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
