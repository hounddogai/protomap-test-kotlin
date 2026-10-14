package com.acme.transport.emissions.emissionfactor.digest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class EmissionFactorBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEmissionFactorBundleEngine(newRegistry())
        val item = newEmissionFactorBundle("code-91", "title-102")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
