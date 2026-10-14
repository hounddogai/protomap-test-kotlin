package com.acme.transport.lastmile.proofofdelivery.cycle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedProofOfDeliveryReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedProofOfDeliveryReviewEngine(newRegistry())
        val item = newBatchedProofOfDeliveryReview("code-875", "name-226")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
