package com.acme.transport.safety.nearmiss.profile

import kotlin.test.Test
import kotlin.test.assertTrue

class NearMissReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultNearMissReviewEngine()
        val item = newNearMissReview("code-935", "name-141")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
