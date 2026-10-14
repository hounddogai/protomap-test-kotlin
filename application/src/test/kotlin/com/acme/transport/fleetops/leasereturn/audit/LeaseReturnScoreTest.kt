package com.acme.transport.fleetops.leasereturn.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LeaseReturnScoreTest {
    @Test
    fun `starts in planned`() {
        val item = newLeaseReturnScore("code-562", "name-813")
        assertEquals(LeaseReturnScoreStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newLeaseReturnScore("code-562", "name-813")
        try {
            item.moveTo(LeaseReturnScoreStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LeaseReturnScoreStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLeaseReturnScore("code-562", "name-813")
        item.status = LeaseReturnScoreStatus.CANCELLED
        assertFails("move") { item.moveTo(LeaseReturnScoreStatus.PLANNED) }
    }
}
