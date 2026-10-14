package com.acme.transport.fleetops.leasereturn.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LeaseReturnEntryTest {
    @Test
    fun `starts in requested`() {
        val item = newLeaseReturnEntry("code-751", "title-707")
        assertEquals(LeaseReturnEntryStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newLeaseReturnEntry("code-751", "title-707")
        try {
            item.moveTo(LeaseReturnEntryStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LeaseReturnEntryStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLeaseReturnEntry("code-751", "title-707")
        item.status = LeaseReturnEntryStatus.EXPIRED
        assertFails("move") { item.moveTo(LeaseReturnEntryStatus.REQUESTED) }
    }
}
