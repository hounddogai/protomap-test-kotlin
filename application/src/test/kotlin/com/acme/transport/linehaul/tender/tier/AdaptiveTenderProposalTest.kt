package com.acme.transport.linehaul.tender.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveTenderProposalTest {
    @Test
    fun `starts in draft`() {
        val item = newAdaptiveTenderProposal("code-847", "title-163")
        assertEquals(AdaptiveTenderProposalStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newAdaptiveTenderProposal("code-847", "title-163")
        try {
            item.moveTo(AdaptiveTenderProposalStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveTenderProposalStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newAdaptiveTenderProposal("code-847", "title-163")
        item.status = AdaptiveTenderProposalStatus.CLOSED
        assertFails("move") { item.moveTo(AdaptiveTenderProposalStatus.DRAFT) }
    }
}
