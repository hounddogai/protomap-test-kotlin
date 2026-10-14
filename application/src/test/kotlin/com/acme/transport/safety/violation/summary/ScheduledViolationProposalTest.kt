package com.acme.transport.safety.violation.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledViolationProposalTest {
    @Test
    fun `starts in planned`() {
        val item = newScheduledViolationProposal("code-765", "title-46")
        assertEquals(ScheduledViolationProposalStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newScheduledViolationProposal("code-765", "title-46")
        try {
            item.moveTo(ScheduledViolationProposalStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledViolationProposalStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newScheduledViolationProposal("code-765", "title-46")
        item.status = ScheduledViolationProposalStatus.CANCELLED
        assertFails("move") { item.moveTo(ScheduledViolationProposalStatus.PLANNED) }
    }
}
