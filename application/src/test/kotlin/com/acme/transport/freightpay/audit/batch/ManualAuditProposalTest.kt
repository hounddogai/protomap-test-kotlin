package com.acme.transport.freightpay.audit.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualAuditProposalTest {
    @Test
    fun `starts in proposed`() {
        val item = newManualAuditProposal("code-854", "title-52")
        assertEquals(ManualAuditProposalStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newManualAuditProposal("code-854", "title-52")
        try {
            item.moveTo(ManualAuditProposalStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualAuditProposalStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newManualAuditProposal("code-854", "title-52")
        item.status = ManualAuditProposalStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ManualAuditProposalStatus.PROPOSED) }
    }
}
