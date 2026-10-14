package com.acme.transport.lastmile.proofofdelivery.segment

import kotlin.test.Test
import kotlin.test.assertTrue

class PendingProofOfDeliveryPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingProofOfDeliveryPlanEngine()
        val item = newPendingProofOfDeliveryPlan("code-608", "title-993")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
