package com.acme.transport.freightpay.dispute.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingDisputeLedgerTest {
    @Test
    fun `starts in open`() {
        val item = newRollingDisputeLedger("code-2", "name-877")
        assertEquals(RollingDisputeLedgerStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newRollingDisputeLedger("code-2", "name-877")
        try {
            item.moveTo(RollingDisputeLedgerStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingDisputeLedgerStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRollingDisputeLedger("code-2", "name-877")
        item.status = RollingDisputeLedgerStatus.ARCHIVED
        assertFails("move") { item.moveTo(RollingDisputeLedgerStatus.OPEN) }
    }
}
