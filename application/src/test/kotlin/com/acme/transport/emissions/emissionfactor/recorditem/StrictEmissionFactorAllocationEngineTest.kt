package com.acme.transport.emissions.emissionfactor.recorditem

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictEmissionFactorAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictEmissionFactorAllocationEngine()
        val item = newStrictEmissionFactorAllocation("code-706", "title-969")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
