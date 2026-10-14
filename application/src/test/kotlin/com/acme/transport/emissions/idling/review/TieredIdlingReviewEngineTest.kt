package com.acme.transport.emissions.idling.review

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredIdlingReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredIdlingReviewEngine(newRegistry())
        val item = newTieredIdlingReview("code-380", "name-422")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
