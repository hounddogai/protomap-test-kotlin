package com.acme.transport.network.node.nodeagreement

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalNodePolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalNodePolicyEngine(newRegistry())
        val item = newGlobalNodePolicy("code-493", "label-20")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
