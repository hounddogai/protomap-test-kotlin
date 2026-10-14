package com.acme.transport.fleetops.leasereturn.returnmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingLeaseReturnAssignmentTest {
    @Test
    fun `starts in new`() {
        val item = newPendingLeaseReturnAssignment("code-734", "title-219")
        assertEquals(PendingLeaseReturnAssignmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newPendingLeaseReturnAssignment("code-734", "title-219")
        try {
            item.moveTo(PendingLeaseReturnAssignmentStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingLeaseReturnAssignmentStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newPendingLeaseReturnAssignment("code-734", "title-219")
        item.status = PendingLeaseReturnAssignmentStatus.FAILED
        assertFails("move") { item.moveTo(PendingLeaseReturnAssignmentStatus.NEW) }
    }
}
