package com.acme.transport.fleetops.leasereturn.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientLeaseReturnAuditTest {
    @Test
    fun `starts in new`() {
        val item = newLenientLeaseReturnAudit("code-16", "title-669")
        assertEquals(LenientLeaseReturnAuditStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newLenientLeaseReturnAudit("code-16", "title-669")
        try {
            item.moveTo(LenientLeaseReturnAuditStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientLeaseReturnAuditStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLenientLeaseReturnAudit("code-16", "title-669")
        item.status = LenientLeaseReturnAuditStatus.FAILED
        assertFails("move") { item.moveTo(LenientLeaseReturnAuditStatus.NEW) }
    }
}
