package com.acme.transport.freightpay.audit.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AuditProposalTest {
    @Test
    fun `starts in idle`() {
        val item = newAuditProposal("code-958", "name-83")
        assertEquals(AuditProposalStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newAuditProposal("code-958", "name-83")
        try {
            item.moveTo(AuditProposalStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AuditProposalStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAuditProposal("code-958", "name-83")
        item.status = AuditProposalStatus.STOPPED
        assertFails("move") { item.moveTo(AuditProposalStatus.IDLE) }
    }
}
