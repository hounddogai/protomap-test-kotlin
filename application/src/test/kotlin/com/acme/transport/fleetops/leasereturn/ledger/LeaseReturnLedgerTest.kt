package com.acme.transport.fleetops.leasereturn.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LeaseReturnLedgerTest {
    @Test
    fun `starts in planned`() {
        val item = newLeaseReturnLedger("code-165", "title-425")
        assertEquals(LeaseReturnLedgerStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newLeaseReturnLedger("code-165", "title-425")
        try {
            item.moveTo(LeaseReturnLedgerStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LeaseReturnLedgerStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLeaseReturnLedger("code-165", "title-425")
        item.status = LeaseReturnLedgerStatus.CANCELLED
        assertFails("move") { item.moveTo(LeaseReturnLedgerStatus.PLANNED) }
    }
}
