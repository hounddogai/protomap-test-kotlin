package com.acme.transport.fleetops.leasereturn.adaptivereturnledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingLeaseReturnLedgerTest {
    @Test
    fun `starts in planned`() {
        val item = newRollingLeaseReturnLedger("code-714", "label-497")
        assertEquals(RollingLeaseReturnLedgerStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newRollingLeaseReturnLedger("code-714", "label-497")
        try {
            item.moveTo(RollingLeaseReturnLedgerStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingLeaseReturnLedgerStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRollingLeaseReturnLedger("code-714", "label-497")
        item.status = RollingLeaseReturnLedgerStatus.CANCELLED
        assertFails("move") { item.moveTo(RollingLeaseReturnLedgerStatus.PLANNED) }
    }
}
