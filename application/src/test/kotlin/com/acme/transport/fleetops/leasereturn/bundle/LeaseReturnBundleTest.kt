package com.acme.transport.fleetops.leasereturn.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LeaseReturnBundleTest {
    @Test
    fun `starts in planned`() {
        val item = newLeaseReturnBundle("code-983", "name-899")
        assertEquals(LeaseReturnBundleStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newLeaseReturnBundle("code-983", "name-899")
        try {
            item.moveTo(LeaseReturnBundleStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LeaseReturnBundleStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLeaseReturnBundle("code-983", "name-899")
        item.status = LeaseReturnBundleStatus.CANCELLED
        assertFails("move") { item.moveTo(LeaseReturnBundleStatus.PLANNED) }
    }
}
