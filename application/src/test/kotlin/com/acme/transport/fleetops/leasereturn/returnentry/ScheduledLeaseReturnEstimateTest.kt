package com.acme.transport.fleetops.leasereturn.returnentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledLeaseReturnEstimateTest {
    @Test
    fun `starts in new`() {
        val item = newScheduledLeaseReturnEstimate("code-556", "label-727")
        assertEquals(ScheduledLeaseReturnEstimateStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newScheduledLeaseReturnEstimate("code-556", "label-727")
        try {
            item.moveTo(ScheduledLeaseReturnEstimateStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledLeaseReturnEstimateStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newScheduledLeaseReturnEstimate("code-556", "label-727")
        item.status = ScheduledLeaseReturnEstimateStatus.FAILED
        assertFails("move") { item.moveTo(ScheduledLeaseReturnEstimateStatus.NEW) }
    }
}
