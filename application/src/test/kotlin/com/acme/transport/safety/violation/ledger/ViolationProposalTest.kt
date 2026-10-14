package com.acme.transport.safety.violation.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ViolationProposalTest {
    @Test
    fun `starts in open`() {
        val item = newViolationProposal("code-929", "title-143")
        assertEquals(ViolationProposalStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newViolationProposal("code-929", "title-143")
        try {
            item.moveTo(ViolationProposalStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ViolationProposalStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newViolationProposal("code-929", "title-143")
        item.status = ViolationProposalStatus.ARCHIVED
        assertFails("move") { item.moveTo(ViolationProposalStatus.OPEN) }
    }
}
