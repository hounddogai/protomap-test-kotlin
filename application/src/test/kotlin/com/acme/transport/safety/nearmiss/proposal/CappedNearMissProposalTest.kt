package com.acme.transport.safety.nearmiss.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedNearMissProposalTest {
    @Test
    fun `starts in requested`() {
        val item = newCappedNearMissProposal("code-788", "label-220")
        assertEquals(CappedNearMissProposalStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newCappedNearMissProposal("code-788", "label-220")
        try {
            item.moveTo(CappedNearMissProposalStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedNearMissProposalStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCappedNearMissProposal("code-788", "label-220")
        item.status = CappedNearMissProposalStatus.EXPIRED
        assertFails("move") { item.moveTo(CappedNearMissProposalStatus.REQUESTED) }
    }
}
