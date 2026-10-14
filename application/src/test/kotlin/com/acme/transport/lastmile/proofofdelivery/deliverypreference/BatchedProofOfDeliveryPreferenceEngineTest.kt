package com.acme.transport.lastmile.proofofdelivery.deliverypreference

import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedProofOfDeliveryPreferenceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedProofOfDeliveryPreferenceEngine()
        val item = newBatchedProofOfDeliveryPreference("code-326", "label-964")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
