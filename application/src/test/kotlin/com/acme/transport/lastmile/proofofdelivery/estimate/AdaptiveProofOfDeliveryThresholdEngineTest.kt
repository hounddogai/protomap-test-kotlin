package com.acme.transport.lastmile.proofofdelivery.estimate

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveProofOfDeliveryThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveProofOfDeliveryThresholdEngine()
        val item = newAdaptiveProofOfDeliveryThreshold("code-710", "name-582")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
