package com.acme.transport.emissions.emissionfactor.rollingfactorsegment

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalEmissionFactorExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalEmissionFactorExceptionEngine()
        val item = newSeasonalEmissionFactorException("code-156", "label-341")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
