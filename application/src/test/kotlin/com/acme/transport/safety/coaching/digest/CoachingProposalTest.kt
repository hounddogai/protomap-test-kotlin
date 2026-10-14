package com.acme.transport.safety.coaching.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CoachingProposalTest {
    @Test
    fun `starts in requested`() {
        val item = newCoachingProposal("code-617", "name-546")
        assertEquals(CoachingProposalStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newCoachingProposal("code-617", "name-546")
        try {
            item.moveTo(CoachingProposalStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CoachingProposalStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCoachingProposal("code-617", "name-546")
        item.status = CoachingProposalStatus.EXPIRED
        assertFails("move") { item.moveTo(CoachingProposalStatus.REQUESTED) }
    }
}
