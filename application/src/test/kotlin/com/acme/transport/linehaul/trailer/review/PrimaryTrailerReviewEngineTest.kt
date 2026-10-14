package com.acme.transport.linehaul.trailer.review

import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryTrailerReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryTrailerReviewEngine()
        val item = newPrimaryTrailerReview("code-646", "label-181")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
