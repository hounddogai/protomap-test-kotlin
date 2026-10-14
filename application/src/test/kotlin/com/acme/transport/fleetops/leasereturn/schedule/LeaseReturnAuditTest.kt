package com.acme.transport.fleetops.leasereturn.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LeaseReturnAuditTest {
    @Test
    fun `starts in new`() {
        val item = newLeaseReturnAudit("code-360", "label-522")
        assertEquals(LeaseReturnAuditStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newLeaseReturnAudit("code-360", "label-522")
        try {
            item.moveTo(LeaseReturnAuditStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LeaseReturnAuditStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLeaseReturnAudit("code-360", "label-522")
        item.status = LeaseReturnAuditStatus.FAILED
        assertFails("move") { item.moveTo(LeaseReturnAuditStatus.NEW) }
    }
}
