package com.acme.transport.crewing.availability.weightedavailabilityreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AvailabilityProposalTest {
    @Test
    fun `starts in created`() {
        val item = newAvailabilityProposal("code-243", "title-399")
        assertEquals(AvailabilityProposalStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newAvailabilityProposal("code-243", "title-399")
        try {
            item.moveTo(AvailabilityProposalStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AvailabilityProposalStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newAvailabilityProposal("code-243", "title-399")
        item.status = AvailabilityProposalStatus.RETIRED
        assertFails("move") { item.moveTo(AvailabilityProposalStatus.CREATED) }
    }
}
