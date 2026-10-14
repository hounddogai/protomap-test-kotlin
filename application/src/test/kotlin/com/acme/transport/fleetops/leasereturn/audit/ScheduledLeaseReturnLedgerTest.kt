package com.acme.transport.fleetops.leasereturn.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledLeaseReturnLedgerTest {
    @Test
    fun `starts in open`() {
        val item = newScheduledLeaseReturnLedger("code-315", "label-916")
        assertEquals(ScheduledLeaseReturnLedgerStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newScheduledLeaseReturnLedger("code-315", "label-916")
        try {
            item.moveTo(ScheduledLeaseReturnLedgerStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledLeaseReturnLedgerStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newScheduledLeaseReturnLedger("code-315", "label-916")
        item.status = ScheduledLeaseReturnLedgerStatus.ARCHIVED
        assertFails("move") { item.moveTo(ScheduledLeaseReturnLedgerStatus.OPEN) }
    }
}
