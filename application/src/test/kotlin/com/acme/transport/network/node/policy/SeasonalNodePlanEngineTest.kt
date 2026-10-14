package com.acme.transport.network.node.policy

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalNodePlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalNodePlanEngine(newRegistry())
        val item = newSeasonalNodePlan("code-768", "label-888")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
