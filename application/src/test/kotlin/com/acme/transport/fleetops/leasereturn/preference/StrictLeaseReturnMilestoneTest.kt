package com.acme.transport.fleetops.leasereturn.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictLeaseReturnMilestoneTest {
    @Test
    fun `starts in open`() {
        val item = newStrictLeaseReturnMilestone("code-337", "label-455")
        assertEquals(StrictLeaseReturnMilestoneStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newStrictLeaseReturnMilestone("code-337", "label-455")
        try {
            item.moveTo(StrictLeaseReturnMilestoneStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictLeaseReturnMilestoneStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newStrictLeaseReturnMilestone("code-337", "label-455")
        item.status = StrictLeaseReturnMilestoneStatus.ARCHIVED
        assertFails("move") { item.moveTo(StrictLeaseReturnMilestoneStatus.OPEN) }
    }
}
