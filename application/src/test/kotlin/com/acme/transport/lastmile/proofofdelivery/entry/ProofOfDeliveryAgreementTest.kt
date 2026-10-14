package com.acme.transport.lastmile.proofofdelivery.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ProofOfDeliveryAgreementTest {
    @Test
    fun `starts in open`() {
        val item = newProofOfDeliveryAgreement("code-676", "name-417")
        assertEquals(ProofOfDeliveryAgreementStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newProofOfDeliveryAgreement("code-676", "name-417")
        try {
            item.moveTo(ProofOfDeliveryAgreementStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ProofOfDeliveryAgreementStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newProofOfDeliveryAgreement("code-676", "name-417")
        item.status = ProofOfDeliveryAgreementStatus.ARCHIVED
        assertFails("move") { item.moveTo(ProofOfDeliveryAgreementStatus.OPEN) }
    }
}
