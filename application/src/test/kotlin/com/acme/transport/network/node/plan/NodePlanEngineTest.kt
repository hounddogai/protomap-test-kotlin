package com.acme.transport.network.node.plan

import kotlin.test.Test
import kotlin.test.assertTrue

class NodePlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultNodePlanEngine()
        val item = newNodePlan("code-51", "name-302")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
