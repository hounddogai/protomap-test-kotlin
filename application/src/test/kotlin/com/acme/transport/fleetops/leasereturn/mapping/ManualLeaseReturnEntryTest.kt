package com.acme.transport.fleetops.leasereturn.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualLeaseReturnEntryTest {
    @Test
    fun `starts in new`() {
        val item = newManualLeaseReturnEntry("code-442", "label-223")
        assertEquals(ManualLeaseReturnEntryStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newManualLeaseReturnEntry("code-442", "label-223")
        try {
            item.moveTo(ManualLeaseReturnEntryStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualLeaseReturnEntryStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newManualLeaseReturnEntry("code-442", "label-223")
        item.status = ManualLeaseReturnEntryStatus.FAILED
        assertFails("move") { item.moveTo(ManualLeaseReturnEntryStatus.NEW) }
    }
}
