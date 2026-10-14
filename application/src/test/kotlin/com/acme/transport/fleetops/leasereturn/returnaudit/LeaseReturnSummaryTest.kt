package com.acme.transport.fleetops.leasereturn.returnaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LeaseReturnSummaryTest {
    @Test
    fun `starts in idle`() {
        val item = newLeaseReturnSummary("code-744", "label-370")
        assertEquals(LeaseReturnSummaryStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newLeaseReturnSummary("code-744", "label-370")
        try {
            item.moveTo(LeaseReturnSummaryStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LeaseReturnSummaryStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLeaseReturnSummary("code-744", "label-370")
        item.status = LeaseReturnSummaryStatus.STOPPED
        assertFails("move") { item.moveTo(LeaseReturnSummaryStatus.IDLE) }
    }
}
