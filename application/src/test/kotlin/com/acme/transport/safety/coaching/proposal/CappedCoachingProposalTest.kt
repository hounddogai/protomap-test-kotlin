package com.acme.transport.safety.coaching.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedCoachingProposalTest {
    @Test
    fun `starts in open`() {
        val item = newCappedCoachingProposal("code-738", "label-583")
        assertEquals(CappedCoachingProposalStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newCappedCoachingProposal("code-738", "label-583")
        try {
            item.moveTo(CappedCoachingProposalStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedCoachingProposalStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCappedCoachingProposal("code-738", "label-583")
        item.status = CappedCoachingProposalStatus.ARCHIVED
        assertFails("move") { item.moveTo(CappedCoachingProposalStatus.OPEN) }
    }
}
