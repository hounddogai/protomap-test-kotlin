package com.acme.transport.emissions.emissionfactor.cycle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedEmissionFactorRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedEmissionFactorRequestEngine(newRegistry())
        val item = newBatchedEmissionFactorRequest("code-917", "title-874")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
