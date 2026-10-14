package com.acme.transport.freightpay.audit.auditpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictAuditProposalTest {
    @Test
    fun `starts in draft`() {
        val item = newStrictAuditProposal("code-884", "title-110")
        assertEquals(StrictAuditProposalStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newStrictAuditProposal("code-884", "title-110")
        try {
            item.moveTo(StrictAuditProposalStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictAuditProposalStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newStrictAuditProposal("code-884", "title-110")
        item.status = StrictAuditProposalStatus.CLOSED
        assertFails("move") { item.moveTo(StrictAuditProposalStatus.DRAFT) }
    }
}
