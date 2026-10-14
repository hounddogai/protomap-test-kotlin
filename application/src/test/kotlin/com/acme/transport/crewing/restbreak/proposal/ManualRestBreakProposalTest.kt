package com.acme.transport.crewing.restbreak.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualRestBreakProposalTest {
    @Test
    fun `starts in open`() {
        val item = newManualRestBreakProposal("code-739", "title-545")
        assertEquals(ManualRestBreakProposalStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newManualRestBreakProposal("code-739", "title-545")
        try {
            item.moveTo(ManualRestBreakProposalStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualRestBreakProposalStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newManualRestBreakProposal("code-739", "title-545")
        item.status = ManualRestBreakProposalStatus.ARCHIVED
        assertFails("move") { item.moveTo(ManualRestBreakProposalStatus.OPEN) }
    }
}
