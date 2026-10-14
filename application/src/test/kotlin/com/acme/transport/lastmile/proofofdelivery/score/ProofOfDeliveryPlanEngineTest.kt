package com.acme.transport.lastmile.proofofdelivery.score

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ProofOfDeliveryPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultProofOfDeliveryPlanEngine(newRegistry())
        val item = newProofOfDeliveryPlan("code-956", "title-337")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
