package com.acme.transport.emissions.emissionfactor.digest

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedEmissionFactorDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedEmissionFactorDigestEngine()
        val item = newBatchedEmissionFactorDigest("code-681", "title-472")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
