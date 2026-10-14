package com.acme.transport.network.hub.batchedhubmilestone

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveHubMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveHubMilestoneEngine()
        val item = newAdaptiveHubMilestone("code-670", "label-440")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
