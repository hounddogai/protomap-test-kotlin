package com.acme.transport.network.node.template

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class CappedNodeVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedNodeVarianceEngine(newRegistry())
        val item = newCappedNodeVariance("code-532", "name-670")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
