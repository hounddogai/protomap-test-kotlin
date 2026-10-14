package com.acme.transport.lastmile.proofofdelivery.deliveryallocation

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualProofOfDeliveryAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualProofOfDeliveryAdjustmentEngine()
        val item = newManualProofOfDeliveryAdjustment("code-255", "title-998")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
