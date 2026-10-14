package com.acme.transport.lastmile.proofofdelivery.group

import kotlin.test.Test
import kotlin.test.assertTrue

class ProofOfDeliveryGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultProofOfDeliveryGroupEngine()
        val item = newProofOfDeliveryGroup("code-540", "name-69")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
