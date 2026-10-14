package com.acme.transport.linehaul.relay.quota

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveRelayReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveRelayReviewEngine()
        val item = newAdaptiveRelayReview("code-535", "label-31")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
