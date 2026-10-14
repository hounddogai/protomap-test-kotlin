package com.acme.transport.freightpay.accessorial.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalAccessorialProposalTest {
    @Test
    fun `starts in proposed`() {
        val item = newSeasonalAccessorialProposal("code-682", "label-477")
        assertEquals(SeasonalAccessorialProposalStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newSeasonalAccessorialProposal("code-682", "label-477")
        try {
            item.moveTo(SeasonalAccessorialProposalStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalAccessorialProposalStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newSeasonalAccessorialProposal("code-682", "label-477")
        item.status = SeasonalAccessorialProposalStatus.WITHDRAWN
        assertFails("move") { item.moveTo(SeasonalAccessorialProposalStatus.PROPOSED) }
    }
}
