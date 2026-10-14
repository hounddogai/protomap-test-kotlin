package com.acme.transport.network.node.nodedraft

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyNodePlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyNodePlanEngine()
        val item = newLegacyNodePlan("code-365", "name-950")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
