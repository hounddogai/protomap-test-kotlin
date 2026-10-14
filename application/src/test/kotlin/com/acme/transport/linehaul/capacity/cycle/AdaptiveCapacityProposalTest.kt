package com.acme.transport.linehaul.capacity.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveCapacityProposalTest {
    @Test
    fun `starts in requested`() {
        val item = newAdaptiveCapacityProposal("code-596", "name-567")
        assertEquals(AdaptiveCapacityProposalStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newAdaptiveCapacityProposal("code-596", "name-567")
        try {
            item.moveTo(AdaptiveCapacityProposalStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveCapacityProposalStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newAdaptiveCapacityProposal("code-596", "name-567")
        item.status = AdaptiveCapacityProposalStatus.EXPIRED
        assertFails("move") { item.moveTo(AdaptiveCapacityProposalStatus.REQUESTED) }
    }
}
