package com.acme.transport.emissions.emissionfactor.preference

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class EmissionFactorPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEmissionFactorPreferenceEngine(newRegistry())
        val item = newEmissionFactorPreference("code-881", "label-440")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
