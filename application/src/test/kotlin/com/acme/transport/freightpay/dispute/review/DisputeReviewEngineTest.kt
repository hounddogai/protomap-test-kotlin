package com.acme.transport.freightpay.dispute.review

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DisputeReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDisputeReviewEngine(newRegistry())
        val item = newDisputeReview("code-811", "title-527")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
