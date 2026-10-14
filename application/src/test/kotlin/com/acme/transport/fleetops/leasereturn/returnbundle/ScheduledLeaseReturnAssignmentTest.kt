package com.acme.transport.fleetops.leasereturn.returnbundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledLeaseReturnAssignmentTest {
    @Test
    fun `starts in idle`() {
        val item = newScheduledLeaseReturnAssignment("code-865", "name-436")
        assertEquals(ScheduledLeaseReturnAssignmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newScheduledLeaseReturnAssignment("code-865", "name-436")
        try {
            item.moveTo(ScheduledLeaseReturnAssignmentStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledLeaseReturnAssignmentStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newScheduledLeaseReturnAssignment("code-865", "name-436")
        item.status = ScheduledLeaseReturnAssignmentStatus.STOPPED
        assertFails("move") { item.moveTo(ScheduledLeaseReturnAssignmentStatus.IDLE) }
    }
}
