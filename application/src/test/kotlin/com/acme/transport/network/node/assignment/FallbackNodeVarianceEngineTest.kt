package com.acme.transport.network.node.assignment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackNodeVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackNodeVarianceEngine(newRegistry())
        val item = newFallbackNodeVariance("code-685", "label-189")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
