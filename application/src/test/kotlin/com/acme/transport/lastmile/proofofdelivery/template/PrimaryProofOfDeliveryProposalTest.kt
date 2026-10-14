package com.acme.transport.lastmile.proofofdelivery.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryProofOfDeliveryProposalTest {
    @Test
    fun `starts in planned`() {
        val item = newPrimaryProofOfDeliveryProposal("code-516", "title-685")
        assertEquals(PrimaryProofOfDeliveryProposalStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newPrimaryProofOfDeliveryProposal("code-516", "title-685")
        try {
            item.moveTo(PrimaryProofOfDeliveryProposalStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryProofOfDeliveryProposalStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPrimaryProofOfDeliveryProposal("code-516", "title-685")
        item.status = PrimaryProofOfDeliveryProposalStatus.CANCELLED
        assertFails("move") { item.moveTo(PrimaryProofOfDeliveryProposalStatus.PLANNED) }
    }
}
