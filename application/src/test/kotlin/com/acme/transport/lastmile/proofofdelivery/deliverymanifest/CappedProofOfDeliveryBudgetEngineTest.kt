package com.acme.transport.lastmile.proofofdelivery.deliverymanifest

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedProofOfDeliveryBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedProofOfDeliveryBudgetEngine()
        val item = newCappedProofOfDeliveryBudget("code-542", "label-285")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
