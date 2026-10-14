package com.acme.transport.terminal.crossdock.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyCrossDockLedgerTest {
    @Test
    fun `starts in proposed`() {
        val item = newLegacyCrossDockLedger("code-606", "name-885")
        assertEquals(LegacyCrossDockLedgerStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newLegacyCrossDockLedger("code-606", "name-885")
        try {
            item.moveTo(LegacyCrossDockLedgerStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyCrossDockLedgerStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLegacyCrossDockLedger("code-606", "name-885")
        item.status = LegacyCrossDockLedgerStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LegacyCrossDockLedgerStatus.PROPOSED) }
    }
}
