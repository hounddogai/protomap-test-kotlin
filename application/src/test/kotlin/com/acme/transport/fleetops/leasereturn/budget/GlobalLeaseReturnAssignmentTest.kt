package com.acme.transport.fleetops.leasereturn.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalLeaseReturnAssignmentTest {
    @Test
    fun `starts in idle`() {
        val item = newGlobalLeaseReturnAssignment("code-142", "title-578")
        assertEquals(GlobalLeaseReturnAssignmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newGlobalLeaseReturnAssignment("code-142", "title-578")
        try {
            item.moveTo(GlobalLeaseReturnAssignmentStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalLeaseReturnAssignmentStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newGlobalLeaseReturnAssignment("code-142", "title-578")
        item.status = GlobalLeaseReturnAssignmentStatus.STOPPED
        assertFails("move") { item.moveTo(GlobalLeaseReturnAssignmentStatus.IDLE) }
    }
}
