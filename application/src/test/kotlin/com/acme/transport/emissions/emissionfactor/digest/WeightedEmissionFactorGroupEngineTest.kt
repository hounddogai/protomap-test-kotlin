package com.acme.transport.emissions.emissionfactor.digest

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedEmissionFactorGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedEmissionFactorGroupEngine()
        val item = newWeightedEmissionFactorGroup("code-218", "name-45")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
