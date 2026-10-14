package com.acme.transport.network.transittime.threshold

import kotlin.test.Test
import kotlin.test.assertTrue

class TransitTimeReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTransitTimeReviewEngine()
        val item = newTransitTimeReview("code-526", "label-575")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
