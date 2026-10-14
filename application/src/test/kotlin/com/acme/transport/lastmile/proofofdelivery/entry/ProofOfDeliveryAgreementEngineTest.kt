package com.acme.transport.lastmile.proofofdelivery.entry

import kotlin.test.Test
import kotlin.test.assertTrue

class ProofOfDeliveryAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultProofOfDeliveryAgreementEngine()
        val item = newProofOfDeliveryAgreement("code-676", "name-417")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
