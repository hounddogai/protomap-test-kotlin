package com.acme.transport.lastmile.proofofdelivery.deliveryreview

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalProofOfDeliveryReviewEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalProofOfDeliveryReviewEngine(newRegistry())
        val item = newRegionalProofOfDeliveryReview("code-97", "name-763")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
