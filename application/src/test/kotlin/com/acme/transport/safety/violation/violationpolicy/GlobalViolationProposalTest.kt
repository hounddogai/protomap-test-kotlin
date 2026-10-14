package com.acme.transport.safety.violation.violationpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalViolationProposalTest {
    @Test
    fun `starts in draft`() {
        val item = newGlobalViolationProposal("code-597", "name-194")
        assertEquals(GlobalViolationProposalStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newGlobalViolationProposal("code-597", "name-194")
        try {
            item.moveTo(GlobalViolationProposalStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalViolationProposalStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newGlobalViolationProposal("code-597", "name-194")
        item.status = GlobalViolationProposalStatus.CLOSED
        assertFails("move") { item.moveTo(GlobalViolationProposalStatus.DRAFT) }
    }
}
