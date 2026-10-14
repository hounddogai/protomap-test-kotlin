package com.acme.transport.crewing.restbreak.variance

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientRestBreakReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientRestBreakReviewEngine()
        val item = newLenientRestBreakReview("code-984", "name-761")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
