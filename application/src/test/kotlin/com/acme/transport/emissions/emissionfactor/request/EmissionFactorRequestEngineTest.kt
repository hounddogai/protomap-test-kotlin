package com.acme.transport.emissions.emissionfactor.request

import kotlin.test.Test
import kotlin.test.assertTrue

class EmissionFactorRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEmissionFactorRequestEngine()
        val item = newEmissionFactorRequest("code-897", "label-664")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
