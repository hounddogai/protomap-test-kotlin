package com.acme.transport.linehaul.tender.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalTenderProposalTest {
    @Test
    fun `starts in planned`() {
        val item = newSeasonalTenderProposal("code-82", "label-577")
        assertEquals(SeasonalTenderProposalStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newSeasonalTenderProposal("code-82", "label-577")
        try {
            item.moveTo(SeasonalTenderProposalStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalTenderProposalStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newSeasonalTenderProposal("code-82", "label-577")
        item.status = SeasonalTenderProposalStatus.CANCELLED
        assertFails("move") { item.moveTo(SeasonalTenderProposalStatus.PLANNED) }
    }
}
