package com.acme.transport.network.node.batch

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualNodePolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualNodePolicyEngine()
        val item = newManualNodePolicy("code-261", "title-829")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
