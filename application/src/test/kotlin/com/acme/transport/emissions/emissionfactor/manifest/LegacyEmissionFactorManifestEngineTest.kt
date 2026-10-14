package com.acme.transport.emissions.emissionfactor.manifest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyEmissionFactorManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyEmissionFactorManifestEngine(newRegistry())
        val item = newLegacyEmissionFactorManifest("code-871", "name-724")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
