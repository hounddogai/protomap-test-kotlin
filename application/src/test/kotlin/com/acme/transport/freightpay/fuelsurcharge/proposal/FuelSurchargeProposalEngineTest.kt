package com.acme.transport.freightpay.fuelsurcharge.proposal

import kotlin.test.Test
import kotlin.test.assertTrue

class FuelSurchargeProposalEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelSurchargeProposalEngine()
        val item = newFuelSurchargeProposal("code-771", "label-833")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
