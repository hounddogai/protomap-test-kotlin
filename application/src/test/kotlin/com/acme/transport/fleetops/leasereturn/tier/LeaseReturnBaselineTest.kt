package com.acme.transport.fleetops.leasereturn.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LeaseReturnBaselineTest {
    @Test
    fun `starts in new`() {
        val item = newLeaseReturnBaseline("code-631", "label-148")
        assertEquals(LeaseReturnBaselineStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newLeaseReturnBaseline("code-631", "label-148")
        try {
            item.moveTo(LeaseReturnBaselineStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LeaseReturnBaselineStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLeaseReturnBaseline("code-631", "label-148")
        item.status = LeaseReturnBaselineStatus.FAILED
        assertFails("move") { item.moveTo(LeaseReturnBaselineStatus.NEW) }
    }
}
