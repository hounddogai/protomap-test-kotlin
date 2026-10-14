package com.acme.transport.network.hub.primaryhubsummary

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedHubTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedHubTierEngine()
        val item = newBatchedHubTier("code-292", "title-195")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
