package com.acme.transport.lastmile.proofofdelivery.estimate

import kotlin.test.Test
import kotlin.test.assertTrue

class ProofOfDeliveryEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultProofOfDeliveryEstimateEngine()
        val item = newProofOfDeliveryEstimate("code-428", "label-965")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
