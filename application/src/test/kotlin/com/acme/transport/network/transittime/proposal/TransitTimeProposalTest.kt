package com.acme.transport.network.transittime.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TransitTimeProposalTest {
    @Test
    fun `starts in requested`() {
        val item = newTransitTimeProposal("code-136", "name-919")
        assertEquals(TransitTimeProposalStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newTransitTimeProposal("code-136", "name-919")
        try {
            item.moveTo(TransitTimeProposalStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TransitTimeProposalStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTransitTimeProposal("code-136", "name-919")
        item.status = TransitTimeProposalStatus.EXPIRED
        assertFails("move") { item.moveTo(TransitTimeProposalStatus.REQUESTED) }
    }
}
