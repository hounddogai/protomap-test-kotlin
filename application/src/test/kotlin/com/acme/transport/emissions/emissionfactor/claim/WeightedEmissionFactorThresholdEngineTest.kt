package com.acme.transport.emissions.emissionfactor.claim

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedEmissionFactorThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedEmissionFactorThresholdEngine(newRegistry())
        val item = newWeightedEmissionFactorThreshold("code-958", "title-933")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
