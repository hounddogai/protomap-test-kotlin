package com.acme.transport.safety.scorecard.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScorecardProposalTest {
    @Test
    fun `starts in draft`() {
        val item = newScorecardProposal("code-864", "label-895")
        assertEquals(ScorecardProposalStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newScorecardProposal("code-864", "label-895")
        try {
            item.moveTo(ScorecardProposalStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScorecardProposalStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newScorecardProposal("code-864", "label-895")
        item.status = ScorecardProposalStatus.CLOSED
        assertFails("move") { item.moveTo(ScorecardProposalStatus.DRAFT) }
    }
}
