package com.acme.transport.fleetops.leasereturn.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LeaseReturnMilestoneTest {
    @Test
    fun `starts in new`() {
        val item = newLeaseReturnMilestone("code-426", "name-413")
        assertEquals(LeaseReturnMilestoneStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newLeaseReturnMilestone("code-426", "name-413")
        try {
            item.moveTo(LeaseReturnMilestoneStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LeaseReturnMilestoneStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLeaseReturnMilestone("code-426", "name-413")
        item.status = LeaseReturnMilestoneStatus.FAILED
        assertFails("move") { item.moveTo(LeaseReturnMilestoneStatus.NEW) }
    }
}
