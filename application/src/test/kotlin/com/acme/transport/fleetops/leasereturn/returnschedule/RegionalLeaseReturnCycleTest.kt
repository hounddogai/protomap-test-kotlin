package com.acme.transport.fleetops.leasereturn.returnschedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalLeaseReturnCycleTest {
    @Test
    fun `starts in proposed`() {
        val item = newRegionalLeaseReturnCycle("code-98", "label-905")
        assertEquals(RegionalLeaseReturnCycleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newRegionalLeaseReturnCycle("code-98", "label-905")
        try {
            item.moveTo(RegionalLeaseReturnCycleStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalLeaseReturnCycleStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRegionalLeaseReturnCycle("code-98", "label-905")
        item.status = RegionalLeaseReturnCycleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RegionalLeaseReturnCycleStatus.PROPOSED) }
    }
}
