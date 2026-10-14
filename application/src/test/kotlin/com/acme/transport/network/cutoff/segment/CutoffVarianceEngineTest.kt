package com.acme.transport.network.cutoff.segment

import kotlin.test.Test
import kotlin.test.assertTrue

class CutoffVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCutoffVarianceEngine()
        val item = newCutoffVariance("code-364", "name-394")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
