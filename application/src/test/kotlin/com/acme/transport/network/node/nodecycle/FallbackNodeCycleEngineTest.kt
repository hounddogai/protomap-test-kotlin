package com.acme.transport.network.node.nodecycle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackNodeCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackNodeCycleEngine(newRegistry())
        val item = newFallbackNodeCycle("code-994", "name-653")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
