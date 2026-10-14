package com.acme.transport.emissions.emissionfactor.preference

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class EmissionFactorPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEmissionFactorPolicyEngine(newRegistry())
        val item = newEmissionFactorPolicy("code-930", "label-361")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
