package com.acme.transport.fleetops.leasereturn.returnschedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LeaseReturnRollupTest {
    @Test
    fun `starts in planned`() {
        val item = newLeaseReturnRollup("code-591", "name-222")
        assertEquals(LeaseReturnRollupStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newLeaseReturnRollup("code-591", "name-222")
        try {
            item.moveTo(LeaseReturnRollupStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LeaseReturnRollupStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLeaseReturnRollup("code-591", "name-222")
        item.status = LeaseReturnRollupStatus.CANCELLED
        assertFails("move") { item.moveTo(LeaseReturnRollupStatus.PLANNED) }
    }
}
