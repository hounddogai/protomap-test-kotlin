package com.acme.transport.emissions.emissionfactor.profile

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedEmissionFactorManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedEmissionFactorManifestEngine(newRegistry())
        val item = newBatchedEmissionFactorManifest("code-583", "label-687")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
