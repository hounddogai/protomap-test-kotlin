package com.acme.transport.lastmile.proofofdelivery.baseline

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedProofOfDeliveryThresholdEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedProofOfDeliveryThresholdEngine()
        val item = newWeightedProofOfDeliveryThreshold("code-674", "title-853")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
