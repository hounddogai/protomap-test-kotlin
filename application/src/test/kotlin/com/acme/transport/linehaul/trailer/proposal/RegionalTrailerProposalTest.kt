package com.acme.transport.linehaul.trailer.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalTrailerProposalTest {
    @Test
    fun `starts in created`() {
        val item = newRegionalTrailerProposal("code-542", "name-934")
        assertEquals(RegionalTrailerProposalStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newRegionalTrailerProposal("code-542", "name-934")
        try {
            item.moveTo(RegionalTrailerProposalStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalTrailerProposalStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRegionalTrailerProposal("code-542", "name-934")
        item.status = RegionalTrailerProposalStatus.RETIRED
        assertFails("move") { item.moveTo(RegionalTrailerProposalStatus.CREATED) }
    }
}
