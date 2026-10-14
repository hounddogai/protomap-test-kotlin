package com.acme.transport.network.node.nodecycle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class NodeVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultNodeVarianceEngine(newRegistry())
        val item = newNodeVariance("code-333", "name-379")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
