package com.acme.transport.fleetops.leasereturn.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LeaseReturnPlanTest {
    @Test
    fun `starts in planned`() {
        val item = newLeaseReturnPlan("code-10", "title-872")
        assertEquals(LeaseReturnPlanStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newLeaseReturnPlan("code-10", "title-872")
        try {
            item.moveTo(LeaseReturnPlanStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LeaseReturnPlanStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLeaseReturnPlan("code-10", "title-872")
        item.status = LeaseReturnPlanStatus.CANCELLED
        assertFails("move") { item.moveTo(LeaseReturnPlanStatus.PLANNED) }
    }
}
