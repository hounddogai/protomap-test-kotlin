package com.acme.transport.emissions.emissionfactor.profile

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class EmissionFactorProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEmissionFactorProfileEngine(newRegistry())
        val item = newEmissionFactorProfile("code-395", "label-401")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
