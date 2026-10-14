package com.acme.transport.linehaul.backhaul.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalBackhaulLedgerTest {
    @Test
    fun `starts in draft`() {
        val item = newGlobalBackhaulLedger("code-501", "name-553")
        assertEquals(GlobalBackhaulLedgerStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newGlobalBackhaulLedger("code-501", "name-553")
        try {
            item.moveTo(GlobalBackhaulLedgerStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalBackhaulLedgerStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newGlobalBackhaulLedger("code-501", "name-553")
        item.status = GlobalBackhaulLedgerStatus.CLOSED
        assertFails("move") { item.moveTo(GlobalBackhaulLedgerStatus.DRAFT) }
    }
}
