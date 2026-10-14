package com.acme.transport.emissions.emissionfactor.factorschedule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class EmissionFactorDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEmissionFactorDigestEngine(newRegistry())
        val item = newEmissionFactorDigest("code-418", "title-736")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
