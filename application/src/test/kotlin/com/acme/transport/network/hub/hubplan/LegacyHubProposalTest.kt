package com.acme.transport.network.hub.hubplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyHubProposalTest {
    @Test
    fun `starts in planned`() {
        val item = newLegacyHubProposal("code-742", "label-771")
        assertEquals(LegacyHubProposalStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newLegacyHubProposal("code-742", "label-771")
        try {
            item.moveTo(LegacyHubProposalStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyHubProposalStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLegacyHubProposal("code-742", "label-771")
        item.status = LegacyHubProposalStatus.CANCELLED
        assertFails("move") { item.moveTo(LegacyHubProposalStatus.PLANNED) }
    }
}
