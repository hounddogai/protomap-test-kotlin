package com.acme.transport.customs.clearance.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingClearanceAssignmentTest {
    @Test
    fun `starts in planned`() {
        val item = newPendingClearanceAssignment("code-242", "label-464")
        assertEquals(PendingClearanceAssignmentStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newPendingClearanceAssignment("code-242", "label-464")
        try {
            item.moveTo(PendingClearanceAssignmentStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingClearanceAssignmentStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPendingClearanceAssignment("code-242", "label-464")
        item.status = PendingClearanceAssignmentStatus.CANCELLED
        assertFails("move") { item.moveTo(PendingClearanceAssignmentStatus.PLANNED) }
    }
}
