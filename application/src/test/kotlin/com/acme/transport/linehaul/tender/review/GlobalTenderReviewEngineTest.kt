package com.acme.transport.linehaul.tender.review

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalTenderReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalTenderReviewEngine()
        val item = newGlobalTenderReview("code-393", "title-911")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
