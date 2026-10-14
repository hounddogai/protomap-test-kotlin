package com.acme.transport.freightpay.dispute.disputeassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualDisputeAssignmentTest {
    @Test
    fun `starts in proposed`() {
        val item = newManualDisputeAssignment("code-250", "label-111")
        assertEquals(ManualDisputeAssignmentStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newManualDisputeAssignment("code-250", "label-111")
        try {
            item.moveTo(ManualDisputeAssignmentStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualDisputeAssignmentStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newManualDisputeAssignment("code-250", "label-111")
        item.status = ManualDisputeAssignmentStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ManualDisputeAssignmentStatus.PROPOSED) }
    }
}
