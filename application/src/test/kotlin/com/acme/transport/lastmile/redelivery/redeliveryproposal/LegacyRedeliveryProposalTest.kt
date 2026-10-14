package com.acme.transport.lastmile.redelivery.redeliveryproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyRedeliveryProposalTest {
    @Test
    fun `starts in open`() {
        val item = newLegacyRedeliveryProposal("code-315", "title-938")
        assertEquals(LegacyRedeliveryProposalStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newLegacyRedeliveryProposal("code-315", "title-938")
        try {
            item.moveTo(LegacyRedeliveryProposalStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyRedeliveryProposalStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLegacyRedeliveryProposal("code-315", "title-938")
        item.status = LegacyRedeliveryProposalStatus.ARCHIVED
        assertFails("move") { item.moveTo(LegacyRedeliveryProposalStatus.OPEN) }
    }
}
