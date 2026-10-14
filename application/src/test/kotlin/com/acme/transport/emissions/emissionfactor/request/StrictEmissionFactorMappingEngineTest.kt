package com.acme.transport.emissions.emissionfactor.request

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictEmissionFactorMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictEmissionFactorMappingEngine(newRegistry())
        val item = newStrictEmissionFactorMapping("code-136", "title-699")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
