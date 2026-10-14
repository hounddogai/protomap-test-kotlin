package com.acme.transport.lastmile.proofofdelivery.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ProofOfDeliveryProposalTest {
    @Test
    fun `starts in created`() {
        val item = newProofOfDeliveryProposal("code-972", "title-715")
        assertEquals(ProofOfDeliveryProposalStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newProofOfDeliveryProposal("code-972", "title-715")
        try {
            item.moveTo(ProofOfDeliveryProposalStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ProofOfDeliveryProposalStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newProofOfDeliveryProposal("code-972", "title-715")
        item.status = ProofOfDeliveryProposalStatus.RETIRED
        assertFails("move") { item.moveTo(ProofOfDeliveryProposalStatus.CREATED) }
    }
}
