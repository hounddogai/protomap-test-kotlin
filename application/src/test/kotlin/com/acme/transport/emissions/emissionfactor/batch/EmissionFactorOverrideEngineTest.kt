package com.acme.transport.emissions.emissionfactor.batch

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class EmissionFactorOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEmissionFactorOverrideEngine(newRegistry())
        val item = newEmissionFactorOverride("code-259", "name-555")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
