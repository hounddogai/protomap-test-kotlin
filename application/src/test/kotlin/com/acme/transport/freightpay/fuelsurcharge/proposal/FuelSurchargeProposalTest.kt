package com.acme.transport.freightpay.fuelsurcharge.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelSurchargeProposalTest {
    @Test
    fun `starts in open`() {
        val item = newFuelSurchargeProposal("code-771", "label-833")
        assertEquals(FuelSurchargeProposalStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newFuelSurchargeProposal("code-771", "label-833")
        try {
            item.moveTo(FuelSurchargeProposalStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelSurchargeProposalStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newFuelSurchargeProposal("code-771", "label-833")
        item.status = FuelSurchargeProposalStatus.ARCHIVED
        assertFails("move") { item.moveTo(FuelSurchargeProposalStatus.OPEN) }
    }
}
