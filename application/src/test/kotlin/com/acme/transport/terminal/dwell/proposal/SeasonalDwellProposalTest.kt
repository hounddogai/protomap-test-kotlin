package com.acme.transport.terminal.dwell.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalDwellProposalTest {
    @Test
    fun `starts in proposed`() {
        val item = newSeasonalDwellProposal("code-54", "title-58")
        assertEquals(SeasonalDwellProposalStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newSeasonalDwellProposal("code-54", "title-58")
        try {
            item.moveTo(SeasonalDwellProposalStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalDwellProposalStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newSeasonalDwellProposal("code-54", "title-58")
        item.status = SeasonalDwellProposalStatus.WITHDRAWN
        assertFails("move") { item.moveTo(SeasonalDwellProposalStatus.PROPOSED) }
    }
}
