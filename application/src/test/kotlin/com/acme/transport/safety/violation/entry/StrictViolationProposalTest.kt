package com.acme.transport.safety.violation.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictViolationProposalTest {
    @Test
    fun `starts in proposed`() {
        val item = newStrictViolationProposal("code-75", "name-104")
        assertEquals(StrictViolationProposalStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newStrictViolationProposal("code-75", "name-104")
        try {
            item.moveTo(StrictViolationProposalStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictViolationProposalStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newStrictViolationProposal("code-75", "name-104")
        item.status = StrictViolationProposalStatus.WITHDRAWN
        assertFails("move") { item.moveTo(StrictViolationProposalStatus.PROPOSED) }
    }
}
