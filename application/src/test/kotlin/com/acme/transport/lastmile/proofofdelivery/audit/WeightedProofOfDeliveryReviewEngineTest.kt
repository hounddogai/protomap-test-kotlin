package com.acme.transport.lastmile.proofofdelivery.audit

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedProofOfDeliveryReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedProofOfDeliveryReviewEngine(newRegistry())
        val item = newWeightedProofOfDeliveryReview("code-571", "label-512")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
