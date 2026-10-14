package com.acme.transport.fleetops.leasereturn.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingLeaseReturnSummaryTest {
    @Test
    fun `starts in planned`() {
        val item = newRollingLeaseReturnSummary("code-497", "title-616")
        assertEquals(RollingLeaseReturnSummaryStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newRollingLeaseReturnSummary("code-497", "title-616")
        try {
            item.moveTo(RollingLeaseReturnSummaryStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingLeaseReturnSummaryStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRollingLeaseReturnSummary("code-497", "title-616")
        item.status = RollingLeaseReturnSummaryStatus.CANCELLED
        assertFails("move") { item.moveTo(RollingLeaseReturnSummaryStatus.PLANNED) }
    }
}
