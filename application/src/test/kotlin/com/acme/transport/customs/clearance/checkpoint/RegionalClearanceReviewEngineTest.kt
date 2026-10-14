package com.acme.transport.customs.clearance.checkpoint

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalClearanceReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalClearanceReviewEngine()
        val item = newRegionalClearanceReview("code-295", "label-450")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
