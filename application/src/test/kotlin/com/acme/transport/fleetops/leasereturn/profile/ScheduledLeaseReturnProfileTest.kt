package com.acme.transport.fleetops.leasereturn.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledLeaseReturnProfileTest {
    @Test
    fun `starts in idle`() {
        val item = newScheduledLeaseReturnProfile("code-395", "title-967")
        assertEquals(ScheduledLeaseReturnProfileStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newScheduledLeaseReturnProfile("code-395", "title-967")
        try {
            item.moveTo(ScheduledLeaseReturnProfileStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledLeaseReturnProfileStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newScheduledLeaseReturnProfile("code-395", "title-967")
        item.status = ScheduledLeaseReturnProfileStatus.STOPPED
        assertFails("move") { item.moveTo(ScheduledLeaseReturnProfileStatus.IDLE) }
    }
}
