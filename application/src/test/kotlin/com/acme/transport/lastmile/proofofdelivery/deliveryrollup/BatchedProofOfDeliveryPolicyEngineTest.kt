package com.acme.transport.lastmile.proofofdelivery.deliveryrollup

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedProofOfDeliveryPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedProofOfDeliveryPolicyEngine(newRegistry())
        val item = newBatchedProofOfDeliveryPolicy("code-25", "title-376")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
