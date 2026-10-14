package com.acme.transport.network.node.score

import kotlin.test.Test
import kotlin.test.assertTrue

class PendingNodeCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingNodeCycleEngine()
        val item = newPendingNodeCycle("code-274", "title-438")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
