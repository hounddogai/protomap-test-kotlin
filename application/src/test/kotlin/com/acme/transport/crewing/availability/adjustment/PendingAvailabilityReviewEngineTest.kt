package com.acme.transport.crewing.availability.adjustment

import kotlin.test.Test
import kotlin.test.assertTrue

class PendingAvailabilityReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingAvailabilityReviewEngine()
        val item = newPendingAvailabilityReview("code-193", "label-952")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
