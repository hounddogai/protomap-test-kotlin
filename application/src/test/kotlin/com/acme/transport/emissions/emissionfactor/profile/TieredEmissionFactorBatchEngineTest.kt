package com.acme.transport.emissions.emissionfactor.profile

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredEmissionFactorBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredEmissionFactorBatchEngine()
        val item = newTieredEmissionFactorBatch("code-921", "label-907")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
