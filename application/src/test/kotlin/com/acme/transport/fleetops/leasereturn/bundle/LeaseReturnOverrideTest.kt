package com.acme.transport.fleetops.leasereturn.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LeaseReturnOverrideTest {
    @Test
    fun `starts in idle`() {
        val item = newLeaseReturnOverride("code-976", "title-112")
        assertEquals(LeaseReturnOverrideStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newLeaseReturnOverride("code-976", "title-112")
        try {
            item.moveTo(LeaseReturnOverrideStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LeaseReturnOverrideStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLeaseReturnOverride("code-976", "title-112")
        item.status = LeaseReturnOverrideStatus.STOPPED
        assertFails("move") { item.moveTo(LeaseReturnOverrideStatus.IDLE) }
    }
}
