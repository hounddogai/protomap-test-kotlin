package com.acme.transport.customs.hold.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HoldProposalTest {
    @Test
    fun `starts in proposed`() {
        val item = newHoldProposal("code-424", "title-164")
        assertEquals(HoldProposalStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newHoldProposal("code-424", "title-164")
        try {
            item.moveTo(HoldProposalStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HoldProposalStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newHoldProposal("code-424", "title-164")
        item.status = HoldProposalStatus.WITHDRAWN
        assertFails("move") { item.moveTo(HoldProposalStatus.PROPOSED) }
    }
}
