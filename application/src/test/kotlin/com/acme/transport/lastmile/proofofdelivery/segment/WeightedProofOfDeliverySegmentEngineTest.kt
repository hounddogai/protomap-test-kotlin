package com.acme.transport.lastmile.proofofdelivery.segment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedProofOfDeliverySegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedProofOfDeliverySegmentEngine(newRegistry())
        val item = newWeightedProofOfDeliverySegment("code-498", "name-95")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
