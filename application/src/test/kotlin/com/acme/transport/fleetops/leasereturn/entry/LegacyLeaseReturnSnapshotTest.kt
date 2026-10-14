package com.acme.transport.fleetops.leasereturn.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyLeaseReturnSnapshotTest {
    @Test
    fun `starts in planned`() {
        val item = newLegacyLeaseReturnSnapshot("code-180", "label-497")
        assertEquals(LegacyLeaseReturnSnapshotStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newLegacyLeaseReturnSnapshot("code-180", "label-497")
        try {
            item.moveTo(LegacyLeaseReturnSnapshotStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyLeaseReturnSnapshotStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLegacyLeaseReturnSnapshot("code-180", "label-497")
        item.status = LegacyLeaseReturnSnapshotStatus.CANCELLED
        assertFails("move") { item.moveTo(LegacyLeaseReturnSnapshotStatus.PLANNED) }
    }
}
