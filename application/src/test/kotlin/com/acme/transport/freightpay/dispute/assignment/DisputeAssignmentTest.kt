package com.acme.transport.freightpay.dispute.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DisputeAssignmentTest {
    @Test
    fun `starts in draft`() {
        val item = newDisputeAssignment("code-970", "label-988")
        assertEquals(DisputeAssignmentStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newDisputeAssignment("code-970", "label-988")
        try {
            item.moveTo(DisputeAssignmentStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DisputeAssignmentStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDisputeAssignment("code-970", "label-988")
        item.status = DisputeAssignmentStatus.CLOSED
        assertFails("move") { item.moveTo(DisputeAssignmentStatus.DRAFT) }
    }
}
