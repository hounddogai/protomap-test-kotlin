package com.acme.transport.fleetops.leasereturn.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LeaseReturnTierTest {
    @Test
    fun `starts in planned`() {
        val item = newLeaseReturnTier("code-531", "label-133")
        assertEquals(LeaseReturnTierStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newLeaseReturnTier("code-531", "label-133")
        try {
            item.moveTo(LeaseReturnTierStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LeaseReturnTierStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLeaseReturnTier("code-531", "label-133")
        item.status = LeaseReturnTierStatus.CANCELLED
        assertFails("move") { item.moveTo(LeaseReturnTierStatus.PLANNED) }
    }
}
