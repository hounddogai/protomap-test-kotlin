package com.acme.transport.fleetops.utilization.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingUtilizationAssignmentTest {
    @Test
    fun `starts in requested`() {
        val item = newPendingUtilizationAssignment("code-540", "label-145")
        assertEquals(PendingUtilizationAssignmentStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newPendingUtilizationAssignment("code-540", "label-145")
        try {
            item.moveTo(PendingUtilizationAssignmentStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingUtilizationAssignmentStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPendingUtilizationAssignment("code-540", "label-145")
        item.status = PendingUtilizationAssignmentStatus.EXPIRED
        assertFails("move") { item.moveTo(PendingUtilizationAssignmentStatus.REQUESTED) }
    }
}
