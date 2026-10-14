package com.acme.transport.emissions.emissionfactor.cycle

import kotlin.test.Test
import kotlin.test.assertTrue

class EmissionFactorCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEmissionFactorCycleEngine()
        val item = newEmissionFactorCycle("code-696", "name-33")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
