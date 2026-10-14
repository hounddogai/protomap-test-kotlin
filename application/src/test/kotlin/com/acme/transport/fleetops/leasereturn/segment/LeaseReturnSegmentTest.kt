package com.acme.transport.fleetops.leasereturn.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LeaseReturnSegmentTest {
    @Test
    fun `starts in created`() {
        val item = newLeaseReturnSegment("code-757", "label-959")
        assertEquals(LeaseReturnSegmentStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newLeaseReturnSegment("code-757", "label-959")
        try {
            item.moveTo(LeaseReturnSegmentStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LeaseReturnSegmentStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLeaseReturnSegment("code-757", "label-959")
        item.status = LeaseReturnSegmentStatus.RETIRED
        assertFails("move") { item.moveTo(LeaseReturnSegmentStatus.CREATED) }
    }
}
