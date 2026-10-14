package com.acme.transport.emissions.emissionfactor.notice

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class EmissionFactorReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEmissionFactorReservationEngine(newRegistry())
        val item = newEmissionFactorReservation("code-668", "title-881")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
