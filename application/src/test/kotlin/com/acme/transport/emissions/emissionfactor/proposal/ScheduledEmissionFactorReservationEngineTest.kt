package com.acme.transport.emissions.emissionfactor.proposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledEmissionFactorReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledEmissionFactorReservationEngine(newRegistry())
        val item = newScheduledEmissionFactorReservation("code-334", "title-921")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
