package com.acme.transport.emissions.emissionfactor.factortarget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackEmissionFactorReservationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackEmissionFactorReservationEngine(newRegistry())
        val item = newFallbackEmissionFactorReservation("code-1", "name-801")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
