package com.acme.transport.emissions.emissionfactor.preference

import kotlin.test.Test
import kotlin.test.assertTrue

class EmissionFactorRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEmissionFactorRollupEngine()
        val item = newEmissionFactorRollup("code-476", "title-232")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
