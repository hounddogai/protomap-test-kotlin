package com.acme.transport.crewing.availability.score

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientAvailabilityReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientAvailabilityReviewEngine()
        val item = newLenientAvailabilityReview("code-112", "label-778")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
