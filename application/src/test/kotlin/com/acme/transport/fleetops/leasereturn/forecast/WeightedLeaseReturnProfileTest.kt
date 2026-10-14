package com.acme.transport.fleetops.leasereturn.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedLeaseReturnProfileTest {
    @Test
    fun `starts in planned`() {
        val item = newWeightedLeaseReturnProfile("code-800", "label-821")
        assertEquals(WeightedLeaseReturnProfileStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newWeightedLeaseReturnProfile("code-800", "label-821")
        try {
            item.moveTo(WeightedLeaseReturnProfileStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedLeaseReturnProfileStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newWeightedLeaseReturnProfile("code-800", "label-821")
        item.status = WeightedLeaseReturnProfileStatus.CANCELLED
        assertFails("move") { item.moveTo(WeightedLeaseReturnProfileStatus.PLANNED) }
    }
}
