package com.acme.transport.fleetops.leasereturn.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LeaseReturnCycleTest {
    @Test
    fun `starts in planned`() {
        val item = newLeaseReturnCycle("code-656", "label-831")
        assertEquals(LeaseReturnCycleStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newLeaseReturnCycle("code-656", "label-831")
        try {
            item.moveTo(LeaseReturnCycleStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LeaseReturnCycleStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLeaseReturnCycle("code-656", "label-831")
        item.status = LeaseReturnCycleStatus.CANCELLED
        assertFails("move") { item.moveTo(LeaseReturnCycleStatus.PLANNED) }
    }
}
