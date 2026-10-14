package com.acme.transport.fleetops.leasereturn.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LeaseReturnAllocationTest {
    @Test
    fun `starts in draft`() {
        val item = newLeaseReturnAllocation("code-65", "title-914")
        assertEquals(LeaseReturnAllocationStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newLeaseReturnAllocation("code-65", "title-914")
        try {
            item.moveTo(LeaseReturnAllocationStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LeaseReturnAllocationStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLeaseReturnAllocation("code-65", "title-914")
        item.status = LeaseReturnAllocationStatus.CLOSED
        assertFails("move") { item.moveTo(LeaseReturnAllocationStatus.DRAFT) }
    }
}
