package com.acme.transport.emissions.emissionfactor.plan

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class EmissionFactorThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEmissionFactorThresholdEngine(newRegistry())
        val item = newEmissionFactorThreshold("code-218", "label-298")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
