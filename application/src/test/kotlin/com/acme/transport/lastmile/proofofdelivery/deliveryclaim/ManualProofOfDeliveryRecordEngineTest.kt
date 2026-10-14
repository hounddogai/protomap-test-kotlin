package com.acme.transport.lastmile.proofofdelivery.deliveryclaim

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualProofOfDeliveryRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultManualProofOfDeliveryRecordEngine()
        val item = newManualProofOfDeliveryRecord("code-429", "title-870")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
