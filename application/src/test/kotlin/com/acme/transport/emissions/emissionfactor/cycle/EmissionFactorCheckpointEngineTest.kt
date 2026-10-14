package com.acme.transport.emissions.emissionfactor.cycle

import kotlin.test.Test
import kotlin.test.assertTrue

class EmissionFactorCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEmissionFactorCheckpointEngine()
        val item = newEmissionFactorCheckpoint("code-469", "label-922")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
