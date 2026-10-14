package com.acme.transport.network.cutoff.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualCutoffLedgerTest {
    @Test
    fun `starts in draft`() {
        val item = newManualCutoffLedger("code-745", "label-749")
        assertEquals(ManualCutoffLedgerStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newManualCutoffLedger("code-745", "label-749")
        try {
            item.moveTo(ManualCutoffLedgerStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualCutoffLedgerStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newManualCutoffLedger("code-745", "label-749")
        item.status = ManualCutoffLedgerStatus.CLOSED
        assertFails("move") { item.moveTo(ManualCutoffLedgerStatus.DRAFT) }
    }
}
