package com.acme.transport.linehaul.backhaul.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveBackhaulProposalTest {
    @Test
    fun `starts in proposed`() {
        val item = newAdaptiveBackhaulProposal("code-694", "title-793")
        assertEquals(AdaptiveBackhaulProposalStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newAdaptiveBackhaulProposal("code-694", "title-793")
        try {
            item.moveTo(AdaptiveBackhaulProposalStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveBackhaulProposalStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newAdaptiveBackhaulProposal("code-694", "title-793")
        item.status = AdaptiveBackhaulProposalStatus.WITHDRAWN
        assertFails("move") { item.moveTo(AdaptiveBackhaulProposalStatus.PROPOSED) }
    }
}
