package com.acme.transport.freightpay.fuelsurcharge.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedFuelSurchargeProposalTest {
    @Test
    fun `starts in requested`() {
        val item = newWeightedFuelSurchargeProposal("code-753", "name-671")
        assertEquals(WeightedFuelSurchargeProposalStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newWeightedFuelSurchargeProposal("code-753", "name-671")
        try {
            item.moveTo(WeightedFuelSurchargeProposalStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedFuelSurchargeProposalStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newWeightedFuelSurchargeProposal("code-753", "name-671")
        item.status = WeightedFuelSurchargeProposalStatus.EXPIRED
        assertFails("move") { item.moveTo(WeightedFuelSurchargeProposalStatus.REQUESTED) }
    }
}
