package com.acme.transport.emissions.emissionfactor.batch

import kotlin.test.Test
import kotlin.test.assertTrue

class EmissionFactorManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEmissionFactorManifestEngine()
        val item = newEmissionFactorManifest("code-91", "title-466")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
