package com.acme.transport.emissions.evcharge.review

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveEvChargeReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveEvChargeReviewEngine()
        val item = newAdaptiveEvChargeReview("code-348", "label-201")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
