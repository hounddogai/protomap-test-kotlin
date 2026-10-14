package com.acme.transport.network.hub.batch

import kotlin.test.Test
import kotlin.test.assertTrue

class HubEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultHubEstimateEngine()
        val item = newHubEstimate("code-28", "label-101")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
