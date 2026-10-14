package com.acme.transport.fleetops.leasereturn.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LeaseReturnProfileTest {
    @Test
    fun `starts in draft`() {
        val item = newLeaseReturnProfile("code-713", "label-714")
        assertEquals(LeaseReturnProfileStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newLeaseReturnProfile("code-713", "label-714")
        try {
            item.moveTo(LeaseReturnProfileStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LeaseReturnProfileStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLeaseReturnProfile("code-713", "label-714")
        item.status = LeaseReturnProfileStatus.CLOSED
        assertFails("move") { item.moveTo(LeaseReturnProfileStatus.DRAFT) }
    }
}
