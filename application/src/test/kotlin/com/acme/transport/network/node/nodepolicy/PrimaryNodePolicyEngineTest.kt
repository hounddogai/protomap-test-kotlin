package com.acme.transport.network.node.nodepolicy

import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryNodePolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryNodePolicyEngine()
        val item = newPrimaryNodePolicy("code-608", "title-256")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
