package com.acme.transport.lastmile.proofofdelivery.review

import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultProofOfDeliveryLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultProofOfDeliveryLedgerEngine()
        val item = newDefaultProofOfDeliveryLedger("code-414", "title-423")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
