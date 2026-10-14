package com.acme.transport.emissions.emissionfactor.metric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class EmissionFactorSnapshotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEmissionFactorSnapshotEngine(newRegistry())
        val item = newEmissionFactorSnapshot("code-456", "name-986")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
