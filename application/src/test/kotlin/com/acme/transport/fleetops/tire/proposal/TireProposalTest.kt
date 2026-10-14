package com.acme.transport.fleetops.tire.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TireProposalTest {
    @Test
    fun `starts in requested`() {
        val item = newTireProposal("code-268", "label-673")
        assertEquals(TireProposalStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newTireProposal("code-268", "label-673")
        try {
            item.moveTo(TireProposalStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TireProposalStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTireProposal("code-268", "label-673")
        item.status = TireProposalStatus.EXPIRED
        assertFails("move") { item.moveTo(TireProposalStatus.REQUESTED) }
    }
}
