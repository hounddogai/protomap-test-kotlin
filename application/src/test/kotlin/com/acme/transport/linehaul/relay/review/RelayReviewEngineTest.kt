package com.acme.transport.linehaul.relay.review

import kotlin.test.Test
import kotlin.test.assertTrue

class RelayReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRelayReviewEngine()
        val item = newRelayReview("code-879", "name-520")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
