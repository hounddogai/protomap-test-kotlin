package com.acme.transport.lastmile.proofofdelivery.budget

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedProofOfDeliveryBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedProofOfDeliveryBudgetEngine(newRegistry())
        val item = newBatchedProofOfDeliveryBudget("code-229", "label-866")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
