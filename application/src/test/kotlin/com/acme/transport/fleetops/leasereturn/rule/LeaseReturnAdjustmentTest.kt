package com.acme.transport.fleetops.leasereturn.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LeaseReturnAdjustmentTest {
    @Test
    fun `starts in proposed`() {
        val item = newLeaseReturnAdjustment("code-724", "name-467")
        assertEquals(LeaseReturnAdjustmentStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newLeaseReturnAdjustment("code-724", "name-467")
        try {
            item.moveTo(LeaseReturnAdjustmentStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LeaseReturnAdjustmentStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLeaseReturnAdjustment("code-724", "name-467")
        item.status = LeaseReturnAdjustmentStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LeaseReturnAdjustmentStatus.PROPOSED) }
    }
}
