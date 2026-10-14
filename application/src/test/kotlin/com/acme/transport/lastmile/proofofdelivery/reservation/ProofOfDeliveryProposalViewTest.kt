package com.acme.transport.lastmile.proofofdelivery.reservation

import kotlin.test.Test
import kotlin.test.assertEquals

class ProofOfDeliveryProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newProofOfDeliveryProposal("code-972", "title-715")
        val view = toProofOfDeliveryProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toProofOfDeliveryProposalViews(mutableListOf<ProofOfDeliveryProposal>(item, item)).size, "views")
    }
}
