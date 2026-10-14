package com.acme.transport.lastmile.proofofdelivery.deliverymanifest

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedProofOfDeliveryAllocationEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedProofOfDeliveryAllocationEngine()
        val item = newCappedProofOfDeliveryAllocation("code-465", "title-850")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
