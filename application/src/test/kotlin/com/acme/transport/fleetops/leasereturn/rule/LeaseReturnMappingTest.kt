package com.acme.transport.fleetops.leasereturn.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LeaseReturnMappingTest {
    @Test
    fun `starts in planned`() {
        val item = newLeaseReturnMapping("code-705", "label-137")
        assertEquals(LeaseReturnMappingStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newLeaseReturnMapping("code-705", "label-137")
        try {
            item.moveTo(LeaseReturnMappingStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LeaseReturnMappingStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLeaseReturnMapping("code-705", "label-137")
        item.status = LeaseReturnMappingStatus.CANCELLED
        assertFails("move") { item.moveTo(LeaseReturnMappingStatus.PLANNED) }
    }
}
