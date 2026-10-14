package com.acme.transport.fleetops.leasereturn.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LeaseReturnGroupTest {
    @Test
    fun `starts in idle`() {
        val item = newLeaseReturnGroup("code-625", "label-336")
        assertEquals(LeaseReturnGroupStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newLeaseReturnGroup("code-625", "label-336")
        try {
            item.moveTo(LeaseReturnGroupStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LeaseReturnGroupStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLeaseReturnGroup("code-625", "label-336")
        item.status = LeaseReturnGroupStatus.STOPPED
        assertFails("move") { item.moveTo(LeaseReturnGroupStatus.IDLE) }
    }
}
