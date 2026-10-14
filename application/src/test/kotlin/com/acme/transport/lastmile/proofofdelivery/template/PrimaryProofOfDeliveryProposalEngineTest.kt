package com.acme.transport.lastmile.proofofdelivery.template

import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryProofOfDeliveryProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryProofOfDeliveryProposalEngine()
        val item = newPrimaryProofOfDeliveryProposal("code-516", "title-685")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
