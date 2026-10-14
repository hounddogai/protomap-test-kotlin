package com.acme.transport.emissions.emissionfactor.manifest

import kotlin.test.Test
import kotlin.test.assertTrue

class EmissionFactorSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEmissionFactorSlotEngine()
        val item = newEmissionFactorSlot("code-609", "title-942")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
