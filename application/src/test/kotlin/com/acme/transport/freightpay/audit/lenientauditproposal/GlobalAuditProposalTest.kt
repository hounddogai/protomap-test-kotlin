package com.acme.transport.freightpay.audit.lenientauditproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalAuditProposalTest {
    @Test
    fun `starts in requested`() {
        val item = newGlobalAuditProposal("code-434", "title-543")
        assertEquals(GlobalAuditProposalStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newGlobalAuditProposal("code-434", "title-543")
        try {
            item.moveTo(GlobalAuditProposalStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalAuditProposalStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newGlobalAuditProposal("code-434", "title-543")
        item.status = GlobalAuditProposalStatus.EXPIRED
        assertFails("move") { item.moveTo(GlobalAuditProposalStatus.REQUESTED) }
    }
}
