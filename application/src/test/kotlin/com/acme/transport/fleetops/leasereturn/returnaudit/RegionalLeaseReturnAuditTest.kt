package com.acme.transport.fleetops.leasereturn.returnaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalLeaseReturnAuditTest {
    @Test
    fun `starts in draft`() {
        val item = newRegionalLeaseReturnAudit("code-692", "name-531")
        assertEquals(RegionalLeaseReturnAuditStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newRegionalLeaseReturnAudit("code-692", "name-531")
        try {
            item.moveTo(RegionalLeaseReturnAuditStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalLeaseReturnAuditStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRegionalLeaseReturnAudit("code-692", "name-531")
        item.status = RegionalLeaseReturnAuditStatus.CLOSED
        assertFails("move") { item.moveTo(RegionalLeaseReturnAuditStatus.DRAFT) }
    }
}
