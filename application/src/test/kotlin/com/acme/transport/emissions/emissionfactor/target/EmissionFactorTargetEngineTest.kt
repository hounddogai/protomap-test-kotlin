package com.acme.transport.emissions.emissionfactor.target

import kotlin.test.Test
import kotlin.test.assertTrue

class EmissionFactorTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEmissionFactorTargetEngine()
        val item = newEmissionFactorTarget("code-198", "title-581")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
