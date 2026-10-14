package com.acme.transport.fleetops.leasereturn.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LeaseReturnManifestTest {
    @Test
    fun `starts in planned`() {
        val item = newLeaseReturnManifest("code-650", "label-550")
        assertEquals(LeaseReturnManifestStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newLeaseReturnManifest("code-650", "label-550")
        try {
            item.moveTo(LeaseReturnManifestStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LeaseReturnManifestStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLeaseReturnManifest("code-650", "label-550")
        item.status = LeaseReturnManifestStatus.CANCELLED
        assertFails("move") { item.moveTo(LeaseReturnManifestStatus.PLANNED) }
    }
}
