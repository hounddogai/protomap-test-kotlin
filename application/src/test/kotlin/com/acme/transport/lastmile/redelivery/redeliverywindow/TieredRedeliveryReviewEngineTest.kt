package com.acme.transport.lastmile.redelivery.redeliverywindow

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredRedeliveryReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredRedeliveryReviewEngine()
        val item = newTieredRedeliveryReview("code-873", "label-749")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
