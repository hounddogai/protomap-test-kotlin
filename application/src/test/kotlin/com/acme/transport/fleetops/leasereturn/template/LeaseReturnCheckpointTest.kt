package com.acme.transport.fleetops.leasereturn.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LeaseReturnCheckpointTest {
    @Test
    fun `starts in new`() {
        val item = newLeaseReturnCheckpoint("code-869", "title-605")
        assertEquals(LeaseReturnCheckpointStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newLeaseReturnCheckpoint("code-869", "title-605")
        try {
            item.moveTo(LeaseReturnCheckpointStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LeaseReturnCheckpointStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLeaseReturnCheckpoint("code-869", "title-605")
        item.status = LeaseReturnCheckpointStatus.FAILED
        assertFails("move") { item.moveTo(LeaseReturnCheckpointStatus.NEW) }
    }
}
