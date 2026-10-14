package com.acme.transport.crewing.availability.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalAvailabilityProposalTest {
    @Test
    fun `starts in planned`() {
        val item = newGlobalAvailabilityProposal("code-487", "label-947")
        assertEquals(GlobalAvailabilityProposalStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newGlobalAvailabilityProposal("code-487", "label-947")
        try {
            item.moveTo(GlobalAvailabilityProposalStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalAvailabilityProposalStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newGlobalAvailabilityProposal("code-487", "label-947")
        item.status = GlobalAvailabilityProposalStatus.CANCELLED
        assertFails("move") { item.moveTo(GlobalAvailabilityProposalStatus.PLANNED) }
    }
}
