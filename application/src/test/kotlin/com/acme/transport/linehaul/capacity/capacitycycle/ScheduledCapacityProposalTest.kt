package com.acme.transport.linehaul.capacity.capacitycycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledCapacityProposalTest {
    @Test
    fun `starts in planned`() {
        val item = newScheduledCapacityProposal("code-3", "label-599")
        assertEquals(ScheduledCapacityProposalStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newScheduledCapacityProposal("code-3", "label-599")
        try {
            item.moveTo(ScheduledCapacityProposalStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledCapacityProposalStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newScheduledCapacityProposal("code-3", "label-599")
        item.status = ScheduledCapacityProposalStatus.CANCELLED
        assertFails("move") { item.moveTo(ScheduledCapacityProposalStatus.PLANNED) }
    }
}
