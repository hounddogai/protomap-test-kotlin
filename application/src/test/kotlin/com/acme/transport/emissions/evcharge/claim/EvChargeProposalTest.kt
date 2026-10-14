package com.acme.transport.emissions.evcharge.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EvChargeProposalTest {
    @Test
    fun `starts in open`() {
        val item = newEvChargeProposal("code-796", "label-477")
        assertEquals(EvChargeProposalStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newEvChargeProposal("code-796", "label-477")
        try {
            item.moveTo(EvChargeProposalStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EvChargeProposalStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newEvChargeProposal("code-796", "label-477")
        item.status = EvChargeProposalStatus.ARCHIVED
        assertFails("move") { item.moveTo(EvChargeProposalStatus.OPEN) }
    }
}
