package com.acme.transport.emissions.emissionfactor.entry

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveEmissionFactorEntryEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveEmissionFactorEntryEngine()
        val item = newAdaptiveEmissionFactorEntry("code-158", "title-713")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
