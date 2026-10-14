package com.acme.transport.lastmile.proofofdelivery.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ProofOfDeliveryAuditTest {
    @Test
    fun `starts in requested`() {
        val item = newProofOfDeliveryAudit("code-889", "label-247")
        assertEquals(ProofOfDeliveryAuditStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newProofOfDeliveryAudit("code-889", "label-247")
        try {
            item.moveTo(ProofOfDeliveryAuditStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ProofOfDeliveryAuditStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newProofOfDeliveryAudit("code-889", "label-247")
        item.status = ProofOfDeliveryAuditStatus.EXPIRED
        assertFails("move") { item.moveTo(ProofOfDeliveryAuditStatus.REQUESTED) }
    }
}
