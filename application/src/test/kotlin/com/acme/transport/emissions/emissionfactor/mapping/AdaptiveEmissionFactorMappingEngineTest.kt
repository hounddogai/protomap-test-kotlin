package com.acme.transport.emissions.emissionfactor.mapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveEmissionFactorMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveEmissionFactorMappingEngine(newRegistry())
        val item = newAdaptiveEmissionFactorMapping("code-795", "name-784")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
