package com.acme.transport.linehaul.laneplan.item

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackLanePlanReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackLanePlanReviewEngine()
        val item = newFallbackLanePlanReview("code-948", "label-304")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
